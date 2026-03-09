import * as vscode from 'vscode';
import * as path from 'path';
import { readdir } from 'fs/promises';
import { execFile } from 'child_process';

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
      let filePath: string;
      if (!workspaceFolder) {
        filePath = doc.fileName;
      } else {
        const basename = path.basename(doc.fileName);
        const matches = await vscode.workspace.findFiles(`**/${basename}`, null, 2);
        filePath = matches.length > 1
          ? vscode.workspace.asRelativePath(doc.uri)
          : basename;
      }

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

      await vscode.env.clipboard.writeText(ref);

      // Find Kitty socket (has PID suffix like kitty-socket-12345)
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

      // Send text directly to Kitty via remote control, then focus it
      execFile('kitty', ['@', '--to', socket, 'send-text', '--', ref], (err) => {
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
  );

  context.subscriptions.push(disposable);
}

export function deactivate() {}
