# claude-code-ref v2.0 Rewrite Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Rewrite the VSCode extension from scratch — single Alt+K command sends absolute-path code reference to Kitty terminal with rich user feedback.

**Architecture:** Single-file extension (`src/extension.ts`) with 5 functions: `findKittySocket`, `getActiveTabInfo`, `sendToKitty`, `sendReference`, `activate/deactivate`. All paths are absolute. User feedback shows target tab position + title on success, diagnostic messages on failure.

**Tech Stack:** TypeScript, VSCode Extension API, Node.js `child_process` (execFile), Kitty remote control protocol

---

### Task 1: Rewrite package.json

**Files:**
- Modify: `package.json`

**Step 1: Replace package.json with stripped-down version**

Replace the entire `contributes` section and bump version:

```json
{
  "name": "claude-code-ref",
  "displayName": "Claude Code Reference",
  "description": "Send absolute-path code reference to Kitty terminal for Claude Code CLI",
  "publisher": "Xin",
  "version": "1.0.0",
  "engines": {
    "vscode": "^1.85.0"
  },
  "categories": [
    "Other"
  ],
  "extensionKind": [
    "ui"
  ],
  "activationEvents": [],
  "main": "./out/extension.js",
  "contributes": {
    "commands": [
      {
        "command": "claude-code-ref.sendReference",
        "title": "Send Code Reference to Kitty"
      }
    ],
    "keybindings": [
      {
        "command": "claude-code-ref.sendReference",
        "key": "alt+k",
        "when": "editorTextFocus"
      }
    ]
  },
  "scripts": {
    "compile": "tsc -p ./",
    "package": "vsce package"
  },
  "devDependencies": {
    "@types/node": "^25.3.5",
    "@types/vscode": "^1.85.0",
    "@vscode/vsce": "^3.0.0",
    "typescript": "^5.3.0"
  }
}
```

Key changes:
- Version: `0.3.2` → `1.0.0`
- Description updated
- Commands: 3 → 1 (`sendReference`)
- Keybindings: 3 → 1 (`Alt+K` with `editorTextFocus`)
- Removed: `menus` (explorer context menu), explorer keybinding, toggle keybinding

**Step 2: Verify JSON is valid**

Run: `cd /Users/ouyangzhaoxin/claude-code-ref && node -e "require('./package.json'); console.log('OK')"`
Expected: `OK`

**Step 3: Commit**

```bash
git add package.json
git commit -m "chore: strip package.json to single sendReference command for v1.0.0"
```

---

### Task 2: Write extension.ts — Kitty integration functions

**Files:**
- Create: `src/extension.ts` (overwrite existing)

**Step 1: Write the complete new extension.ts**

Write the full file from scratch. Here is the complete source:

```typescript
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
```

**Step 2: Verify it compiles**

Run: `cd /Users/ouyangzhaoxin/claude-code-ref && npm run compile`
Expected: No errors

**Step 3: Commit**

```bash
git add src/extension.ts
git commit -m "feat: rewrite extension from scratch — single Alt+K with rich Kitty feedback"
```

---

### Task 3: Manual end-to-end test

**Step 1: Launch extension in development mode**

1. Open the project in VSCode: `code /Users/ouyangzhaoxin/claude-code-ref`
2. Press `F5` to launch Extension Development Host
3. Ensure Kitty is running with remote control enabled

**Step 2: Test — single line reference (no selection)**

1. Open any file in the development host
2. Place cursor on line 10 (no selection)
3. Press `Alt+K`
4. **Verify:** Kitty input line shows `@/absolute/path/to/file#10 ` (with trailing space)
5. **Verify:** Status bar shows `✓ Sent @/absolute/path/to/file#10 → tab #N "title"` for 3 seconds
6. **Verify:** Clipboard contains `@/absolute/path/to/file#10` (no trailing space)

**Step 3: Test — selection range**

1. Select lines 5-12 in any file
2. Press `Alt+K`
3. **Verify:** Kitty shows `@/absolute/path/to/file#5-12 `
4. **Verify:** Status bar shows success with tab info

**Step 4: Test — multi-cursor (same file)**

1. Use `Cmd+D` or `Alt+Click` to create multiple cursors/selections
2. Press `Alt+K`
3. **Verify:** All references sent space-separated

**Step 5: Test — Kitty not running**

1. Close Kitty
2. Press `Alt+K` in any file
3. **Verify:** Error notification: `"Cannot find Kitty socket in /tmp..."`
4. **Verify:** Status bar indicator shows `$(warning) Kitty ✗`

**Step 6: Test — status bar indicator**

1. **Verify:** Status bar shows `Kitty ✓` when Kitty is running
2. Close Kitty, wait ~10 seconds
3. **Verify:** Status bar changes to `Kitty ✗`

---

### Task 4: Package and final commit

**Step 1: Clean old build artifacts**

Run: `cd /Users/ouyangzhaoxin/claude-code-ref && rm -f *.vsix`

**Step 2: Build package**

Run: `npm run compile && npm run package`
Expected: Creates `claude-code-ref-1.0.0.vsix`

**Step 3: Final commit**

```bash
git add -A
git commit -m "chore: build v1.0.0 package"
```

---

### Task 5: Update README

**Files:**
- Modify: `README.md`

**Step 1: Rewrite README to match v2.0**

Update to reflect the simplified feature set:
- Single command description
- Updated keyboard shortcut table (only Alt+K)
- Remove Explorer reference section
- Remove path mode toggle section
- Add user feedback description
- Keep Kitty prerequisites section

**Step 2: Commit**

```bash
git add README.md
git commit -m "docs: update README for v1.0.0 simplified rewrite"
```
