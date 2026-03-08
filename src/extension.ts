import * as vscode from 'vscode';
import * as path from 'path';

async function getShortestUniquePath(
  doc: vscode.TextDocument
): Promise<string> {
  const basename = path.basename(doc.fileName);
  const relativePath = vscode.workspace.asRelativePath(doc.uri);

  // Find all workspace files with the same basename
  const matches = await vscode.workspace.findFiles(`**/${basename}`);

  // Unique name — just use basename
  if (matches.length <= 1) {
    return basename;
  }

  // Multiple matches — find shortest unique suffix
  const allRelativePaths = matches.map(uri =>
    vscode.workspace.asRelativePath(uri)
  );
  const parts = relativePath.split('/');

  for (let suffixLen = 1; suffixLen <= parts.length; suffixLen++) {
    const suffix = parts.slice(-suffixLen).join('/');
    const count = allRelativePaths.filter(
      p => p === suffix || p.endsWith('/' + suffix)
    ).length;
    if (count === 1) {
      return suffix;
    }
  }

  // Fallback: full relative path
  return relativePath;
}

export function activate(context: vscode.ExtensionContext) {
  const disposable = vscode.commands.registerCommand(
    'claude-code-ref.copyReference',
    async () => {
      const editor = vscode.window.activeTextEditor;
      if (!editor) {
        vscode.window.showWarningMessage('No active editor');
        return;
      }

      const doc = editor.document;
      const workspaceFolder = vscode.workspace.getWorkspaceFolder(doc.uri);
      const relativePath = workspaceFolder
        ? await getShortestUniquePath(doc)
        : doc.fileName;

      const selection = editor.selection;
      let ref: string;
      let lineInfo: string;

      if (selection.isEmpty) {
        const line = selection.active.line + 1;
        ref = `@${relativePath}#${line}`;
        lineInfo = `#${line}`;
      } else {
        const startLine = selection.start.line + 1;
        let endLine = selection.end.line + 1;
        if (selection.end.character === 0 && selection.end.line > selection.start.line) {
          endLine = selection.end.line;
        }
        ref = `@${relativePath}#${startLine}-${endLine}`;
        lineInfo = `#${startLine}-${endLine}`;
      }

      await vscode.env.clipboard.writeText(ref);

      const fileName = path.basename(doc.fileName);
      const lineCount = selection.isEmpty ? 1 : selection.end.line - selection.start.line + 1;

      if (lineCount > 200) {
        vscode.window.showWarningMessage(`Copied ${fileName}${lineInfo} (${lineCount} lines - large selection)`);
      } else {
        vscode.window.showInformationMessage(`Copied ${fileName}${lineInfo}`);
      }
    }
  );

  context.subscriptions.push(disposable);
}

export function deactivate() {}
