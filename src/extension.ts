import * as vscode from 'vscode';
import { readdir } from 'fs/promises';
import { execFile } from 'child_process';
import { promisify } from 'util';

const execFileAsync = promisify(execFile);

let statusBarItem: vscode.StatusBarItem;
let socketCheckTimer: ReturnType<typeof setInterval> | undefined;

// ── Kitty socket discovery ──────────────────────────────────

async function findKittySocket(): Promise<string | undefined> {
  try {
    const files = await readdir('/tmp');
    const socket = files.find(f => f.startsWith('kitty-socket'));
    return socket ? `/tmp/${socket}` : undefined;
  } catch {
    return undefined;
  }
}

// ── Kitty tab info ──────────────────────────────────────────

interface TabInfo {
  position: number; // 1-based
  title: string;
}

async function getActiveTabInfo(socketPath: string): Promise<TabInfo | undefined> {
  try {
    const { stdout } = await execFileAsync('kitty', [
      '@', '--to', `unix:${socketPath}`, 'ls',
    ]);
    const osWindows: Array<{
      is_active: boolean;
      tabs: Array<{ is_active: boolean; title: string }>;
    }> = JSON.parse(stdout);

    // Find the active OS window, then the active tab within it
    for (const win of osWindows) {
      if (!win.is_active) { continue; }
      for (let i = 0; i < win.tabs.length; i++) {
        if (win.tabs[i].is_active) {
          return { position: i + 1, title: win.tabs[i].title };
        }
      }
    }
    return undefined;
  } catch {
    return undefined;
  }
}

// ── Send text to Kitty ──────────────────────────────────────

interface SendResult {
  success: boolean;
  tabPosition?: number;
  tabTitle?: string;
  error?: string;
}

async function sendToKitty(text: string): Promise<SendResult> {
  const socketPath = await findKittySocket();
  if (!socketPath) {
    return {
      success: false,
      error: 'Cannot find Kitty socket in /tmp. Is Kitty running with remote control enabled?',
    };
  }

  try {
    await execFileAsync('kitty', [
      '@', '--to', `unix:${socketPath}`,
      'send-text', '--match', 'recent:0', '--', text,
    ]);
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : String(err);
    return { success: false, error: `Kitty send-text failed: ${msg}` };
  }

  // Focus Kitty window (best-effort, don't fail if this errors)
  execFile('open', ['-a', 'kitty'], () => {});

  // Get tab info (best-effort)
  const tabInfo = await getActiveTabInfo(socketPath);

  return {
    success: true,
    tabPosition: tabInfo?.position,
    tabTitle: tabInfo?.title,
  };
}

// ── Build reference string ──────────────────────────────────

function buildReference(editor: vscode.TextEditor): string {
  const absPath = editor.document.uri.fsPath;
  const refs: string[] = [];

  for (const sel of editor.selections) {
    if (sel.isEmpty) {
      const line = sel.active.line + 1;
      refs.push(`@${absPath}#${line}`);
    } else {
      const startLine = sel.start.line + 1;
      let endLine = sel.end.line + 1;
      // If selection ends at column 0 of a line, don't include that line
      if (sel.end.character === 0 && sel.end.line > sel.start.line) {
        endLine = sel.end.line;
      }
      refs.push(`@${absPath}#${startLine}-${endLine}`);
    }
  }

  return refs.join(' ') + ' ';
}

// ── Command handler ─────────────────────────────────────────

async function sendReference(): Promise<void> {
  const editor = vscode.window.activeTextEditor;
  if (!editor) {
    vscode.window.showErrorMessage('No active editor. Open a file first.');
    return;
  }

  const refText = buildReference(editor);
  const result = await sendToKitty(refText);

  if (result.success) {
    // Copy to clipboard (best-effort)
    try {
      await vscode.env.clipboard.writeText(refText.trimEnd());
    } catch {
      vscode.window.setStatusBarMessage('⚠ Sent to Kitty but clipboard copy failed', 3000);
      return;
    }

    // Success feedback with tab info
    const tabDesc = result.tabPosition && result.tabTitle
      ? ` → tab #${result.tabPosition} "${result.tabTitle}"`
      : '';
    vscode.window.setStatusBarMessage(`✓ Sent ${refText.trimEnd()}${tabDesc}`, 3000);
  } else {
    // Failure feedback with diagnostic
    const actions = result.error?.includes('socket')
      ? ['Open Setup Guide']
      : [];
    const choice = await vscode.window.showErrorMessage(
      result.error ?? 'Unknown error sending to Kitty',
      ...actions,
    );
    if (choice === 'Open Setup Guide') {
      vscode.env.openExternal(vscode.Uri.parse(
        'https://sw.kovidgoyal.net/kitty/remote-control/'
      ));
    }
  }
}

// ── Status bar: Kitty connection indicator ──────────────────

async function updateKittyStatus(): Promise<void> {
  const socket = await findKittySocket();
  if (socket) {
    statusBarItem.text = '$(terminal) Kitty ✓';
    statusBarItem.tooltip = `Kitty socket: ${socket}`;
  } else {
    statusBarItem.text = '$(warning) Kitty ✗';
    statusBarItem.tooltip = 'Kitty socket not found — is remote control enabled?';
  }
}

// ── Lifecycle ───────────────────────────────────────────────

export function activate(context: vscode.ExtensionContext): void {
  // Status bar
  statusBarItem = vscode.window.createStatusBarItem(
    vscode.StatusBarAlignment.Right, 100,
  );
  statusBarItem.show();
  updateKittyStatus();

  // Check Kitty connection every 10 seconds
  socketCheckTimer = setInterval(updateKittyStatus, 10_000);

  // Register command
  const cmd = vscode.commands.registerCommand(
    'claude-code-ref.sendReference',
    sendReference,
  );

  context.subscriptions.push(cmd, statusBarItem);
}

export function deactivate(): void {
  if (socketCheckTimer) {
    clearInterval(socketCheckTimer);
    socketCheckTimer = undefined;
  }
}
