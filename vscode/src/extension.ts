import * as vscode from 'vscode';
import {
  createClipboardReferenceCopier,
  type ReferenceEditor,
} from './reference';
import {
  createSmartCopyStateController,
  SMART_COPY_CONFIGURATION_FULL_KEY,
  SMART_COPY_CONFIGURATION_KEY,
  SMART_COPY_CONFIGURATION_SECTION,
} from './smartCopyState';

let statusBarItem: vscode.StatusBarItem;
let smartCopyEnabled = false;

// ── VS Code editor adapter ─────────────────────────────────

function toReferenceEditor(
  editor: vscode.TextEditor,
  selections: readonly vscode.Selection[],
): ReferenceEditor {
  return {
    documentPath: editor.document.uri.fsPath,
    selections: selections.map(sel => ({
      isEmpty: sel.isEmpty,
      activeLine: sel.active.line,
      startLine: sel.start.line,
      endLine: sel.end.line,
      endCharacter: sel.end.character,
    })),
  };
}

function getSelectedReferenceEditor(): ReferenceEditor | undefined {
  const editor = vscode.window.activeTextEditor;
  if (!editor) {
    return undefined;
  }

  const selections = editor.selections.filter(sel => !sel.isEmpty);
  if (selections.length === 0) {
    return undefined;
  }

  return toReferenceEditor(editor, selections);
}

// ── Smart Copy state ────────────────────────────────────────

function updateSmartCopyStatus(): void {
  statusBarItem.text = smartCopyEnabled
    ? '$(check) Code Ref ON'
    : '$(circle-slash) Code Ref OFF';
  statusBarItem.tooltip = smartCopyEnabled
    ? 'Smart Copy is enabled. Selected text copies as a code reference.'
    : 'Smart Copy is disabled. Click to enable code-reference copying.';
}

async function setSmartCopyContext(
  key: string,
  enabled: boolean,
): Promise<void> {
  await vscode.commands.executeCommand(
    'setContext',
    key,
    enabled,
  );
}

type NoEditorHandler = () => void | Thenable<void>;
type ClipboardFailureHandler = (error: unknown) => void | Thenable<void>;

function createCopyReferenceCommand(
  onNoEditor?: NoEditorHandler,
  onClipboardFailure?: ClipboardFailureHandler,
): () => Promise<boolean> {
  return createClipboardReferenceCopier({
    getEditor: getSelectedReferenceEditor,
    writeClipboard: text => vscode.env.clipboard.writeText(text),
    onNoEditor: onNoEditor ?? (() => {
      void vscode.window.showInformationMessage(
        'Select text in an editor to copy a code reference.',
      );
    }),
    onClipboardFailure: onClipboardFailure ?? (() => {
      void vscode.window.showErrorMessage(
        'Failed to copy code reference to clipboard.',
      );
    }),
  });
}

function getSelectedText(): string | undefined {
  const editor = vscode.window.activeTextEditor;
  if (!editor) {
    return undefined;
  }

  const selectedTexts = editor.selections
    .filter(selection => !selection.isEmpty)
    .map(selection => editor.document.getText(selection));
  if (selectedTexts.length === 0) {
    return undefined;
  }

  return selectedTexts.join('\n');
}

async function copySelectedText(): Promise<boolean> {
  const selectedText = getSelectedText();
  if (selectedText === undefined) {
    return false;
  }

  try {
    await vscode.env.clipboard.writeText(selectedText);
  } catch (error: unknown) {
    void vscode.window.showErrorMessage(
      'Failed to copy selected text to clipboard.',
    );
    return false;
  }

  return true;
}

// ── Lifecycle ───────────────────────────────────────────────

export function activate(context: vscode.ExtensionContext): void {
  statusBarItem = vscode.window.createStatusBarItem(
    vscode.StatusBarAlignment.Right, 100,
  );
  statusBarItem.command = 'code-ref.toggleSmartCopy';
  statusBarItem.show();

  const stateController = createSmartCopyStateController({
    getEnabled: () => vscode.workspace
      .getConfiguration(SMART_COPY_CONFIGURATION_SECTION)
      .get<boolean>(SMART_COPY_CONFIGURATION_KEY, false),
    updateEnabled: enabled => vscode.workspace
      .getConfiguration(SMART_COPY_CONFIGURATION_SECTION)
      .update(
        SMART_COPY_CONFIGURATION_KEY,
        enabled,
        vscode.ConfigurationTarget.Global,
      ),
    setContext: setSmartCopyContext,
    updateStatus: enabled => {
      smartCopyEnabled = enabled;
      updateSmartCopyStatus();
    },
  });

  void stateController.syncFromConfiguration();

  const copyCmd = vscode.commands.registerCommand(
    'code-ref.copyReference',
    createCopyReferenceCommand(),
  );
  const copyReferenceForToggle = createCopyReferenceCommand(
    () => undefined,
    () => {
      void vscode.window.showErrorMessage(
        'Failed to copy code reference to clipboard.',
      );
    },
  );
  const toggleCmd = vscode.commands.registerCommand(
    'code-ref.toggleSmartCopy',
    async () => {
      const enabled = await stateController.toggle();
      if (enabled) {
        await copyReferenceForToggle();
      } else {
        await copySelectedText();
      }
      return enabled;
    },
  );
  const configurationListener = vscode.workspace.onDidChangeConfiguration(event => {
    if (event.affectsConfiguration(SMART_COPY_CONFIGURATION_FULL_KEY)) {
      void stateController.syncFromConfiguration();
    }
  });

  context.subscriptions.push(
    copyCmd,
    toggleCmd,
    configurationListener,
    statusBarItem,
  );
}

export function deactivate(): void {
  if (statusBarItem) {
    statusBarItem.dispose();
  }
}
