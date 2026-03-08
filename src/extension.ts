import * as vscode from 'vscode';
import * as path from 'path';

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
        ? vscode.workspace.asRelativePath(doc.uri)
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
