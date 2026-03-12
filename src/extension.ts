import * as vscode from 'vscode';
import * as path from 'path';
import { readdir } from 'fs/promises';
import { execFile } from 'child_process';

/** When true, all paths use absolute form. */
let useAbsolutePath = false;
let statusBarItem: vscode.StatusBarItem;

/**
 * Copy ref to clipboard, send to Kitty terminal, and focus Kitty.
 */
async function sendRef(ref: string): Promise<void> {
  await vscode.env.clipboard.writeText(ref);

  let socketFile: string | undefined;
  try {
    const files = await readdir('/tmp');
    socketFile = files.find(f => f.startsWith('kitty-socket'));
  } catch {
    vscode.window.showErrorMessage('Failed to scan /tmp for Kitty socket.');
    return;
  }
  if (!socketFile) {
    vscode.window.showErrorMessage('Kitty socket not found. Is allow_remote_control enabled?');
    return;
  }
  const socket = `unix:/tmp/${socketFile}`;

  execFile('kitty', ['@', '--to', socket, 'send-text', '--match', 'recent:0', '--', ref], (err) => {
    if (err) {
      vscode.window.showErrorMessage(`Kitty send-text failed: ${err.message}`);
    }
  });
  execFile('open', ['-a', 'kitty'], (err) => {
    if (err) {
      vscode.window.showWarningMessage(`Could not focus Kitty: ${err.message}`);
    }
  });
}

/**
 * Smart file path: basename if unique across workspace, relative path otherwise.
 */
async function getSmartFilePath(
  uri: vscode.Uri,
  workspaceFolder: vscode.WorkspaceFolder | undefined
): Promise<string> {
  if (useAbsolutePath || !workspaceFolder) {
    return uri.fsPath;
  }
  const basename = path.basename(uri.fsPath);
  const matches = await vscode.workspace.findFiles(`**/${basename}`, null, 2);
  return matches.length > 1
    ? vscode.workspace.asRelativePath(uri)
    : basename;
}

/**
 * Smart folder path: folder basename if unique, relative path otherwise.
 * Detects duplicates by searching for files inside any folder with the same name.
 */
async function getSmartFolderPath(
  uri: vscode.Uri,
  workspaceFolder: vscode.WorkspaceFolder | undefined
): Promise<string> {
  if (useAbsolutePath || !workspaceFolder) {
    return uri.fsPath + '/';
  }

  const basename = path.basename(uri.fsPath);

  // Find files directly inside any folder named `basename` to detect duplicates
  const matches = await vscode.workspace.findFiles(`**/${basename}/*`, null, 50);

  const uniqueParents = new Set<string>();
  for (const match of matches) {
    const rel = vscode.workspace.asRelativePath(match);
    const parts = rel.split('/');
    for (let i = 0; i < parts.length; i++) {
      if (parts[i] === basename) {
        uniqueParents.add(parts.slice(0, i + 1).join('/'));
        break;
      }
    }
    if (uniqueParents.size > 1) { break; }
  }

  const pathPart = uniqueParents.size > 1
    ? vscode.workspace.asRelativePath(uri)
    : basename;

  return pathPart + '/';
}

export function activate(context: vscode.ExtensionContext) {
  // ── Status bar: show current path mode ──
  statusBarItem = vscode.window.createStatusBarItem(
    vscode.StatusBarAlignment.Right, 100
  );
  statusBarItem.command = 'claude-code-ref.toggleAbsolutePath';
  statusBarItem.tooltip = 'Click to toggle absolute/relative path mode';
  updateStatusBar();
  statusBarItem.show();

  // ── Toggle absolute path mode ──
  const toggleCmd = vscode.commands.registerCommand(
    'claude-code-ref.toggleAbsolutePath',
    () => {
      useAbsolutePath = !useAbsolutePath;
      updateStatusBar();
      vscode.window.showInformationMessage(
        useAbsolutePath ? 'Path mode: Absolute' : 'Path mode: Smart (relative)'
      );
    }
  );

  // ── Editor: @file#line or @file#startLine-endLine ──
  const editorCmd = vscode.commands.registerCommand(
    'claude-code-ref.copyReference',
    async () => {
      const editor = vscode.window.activeTextEditor;
      if (!editor) {
        vscode.window.showWarningMessage('No active editor');
        return;
      }

      const doc = editor.document;
      const workspaceFolder = vscode.workspace.getWorkspaceFolder(doc.uri);
      const filePath = await getSmartFilePath(doc.uri, workspaceFolder);

      const selection = editor.selection;
      let ref: string;

      if (selection.isEmpty) {
        const line = selection.active.line + 1;
        ref = `@${filePath}#${line}`;
      } else {
        const startLine = selection.start.line + 1;
        let endLine = selection.end.line + 1;
        if (selection.end.character === 0 && selection.end.line > selection.start.line) {
          endLine = selection.end.line;
        }
        ref = `@${filePath}#${startLine}-${endLine}`;
      }

      await sendRef(ref);
    }
  );

  // ── Explorer: @file or @folder/ ──
  const explorerCmd = vscode.commands.registerCommand(
    'claude-code-ref.copyExplorerReference',
    async (uri?: vscode.Uri) => {
      if (!uri) {
        const prev = await vscode.env.clipboard.readText();
        try {
          await vscode.commands.executeCommand('copyFilePath');
          const copied = await vscode.env.clipboard.readText();

          if (!copied || copied === prev) {
            vscode.window.showWarningMessage('No file or folder selected');
            return;
          }
          uri = vscode.Uri.file(copied.split('\n')[0].trim());
        } finally {
          await vscode.env.clipboard.writeText(prev);
        }
      }

      const workspaceFolder = vscode.workspace.getWorkspaceFolder(uri);
      const stat = await vscode.workspace.fs.stat(uri);
      const isFolder = (stat.type & vscode.FileType.Directory) !== 0;

      let refPath: string;
      if (isFolder) {
        refPath = await getSmartFolderPath(uri, workspaceFolder);
      } else {
        refPath = await getSmartFilePath(uri, workspaceFolder);
      }

      const ref = `@${refPath}`;
      await sendRef(ref);
    }
  );

  context.subscriptions.push(toggleCmd, editorCmd, explorerCmd, statusBarItem);
}

function updateStatusBar(): void {
  statusBarItem.text = useAbsolutePath ? '$(file-symlink-file) ABS' : '$(file-code) REL';
}

export function deactivate() {}
