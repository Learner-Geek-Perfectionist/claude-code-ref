# claude-code-ref v2.0 Rewrite Design

Date: 2026-03-14

## Goal

From-scratch rewrite of the VSCode extension. Strip all non-essential features, keep only the core: **one shortcut sends an absolute-path code reference to Kitty terminal with clear user feedback**.

## What Changes

### Removed

- Explorer file/folder reference command (`copyExplorerReference`)
- Smart relative path logic (`getSmartFilePath`, `getSmartFolderPath`)
- Absolute/relative path mode toggle (`toggleAbsolutePath`, `Alt+J`)
- Explorer context menu
- Status bar path mode indicator (ABS/REL)

### Kept

- Kitty remote control integration (send-text via Unix socket)
- `Alt+K` shortcut (editor focus only)
- Clipboard backup
- `open -a kitty` to focus Kitty window

### Added

- Always absolute path (no mode switching)
- Trailing space after reference text
- Rich user feedback: success with target tab info, failure with diagnostic messages
- Status bar Kitty connection indicator
- Target tab identification (position + title) via `kitty @ ls`

## Command Design

One command, one shortcut:

| Command | Keybinding | Behavior |
|---------|------------|----------|
| `sendReference` | `Alt+K` (editor focus) | Generate reference, send to Kitty, copy to clipboard |

### Alt+K Behavior

- No selection: `@/absolute/path/file.c#42 ` (cursor line + trailing space)
- With selection: `@/absolute/path/file.c#7-12 ` (range + trailing space)
- Multi-cursor (same file): generates one reference per selection, space-separated

### Reference Format

Always: `@{absolutePath}#{line}` or `@{absolutePath}#{startLine}-{endLine}`

Trailing space appended for typing convenience.

## User Feedback Design

### Success

Status bar temporary message (3 seconds):

```
✓ Sent @/path/file.c#42 → tab #2 "Claude Code"
```

Where `#2` is the 1-based tab position in the Kitty window, `"Claude Code"` is the tab title.

### Failure Cases

| Scenario | Feedback |
|----------|----------|
| No Kitty socket found | Error notification: `"Cannot find Kitty socket in /tmp. Is Kitty running with remote control enabled?"` with **[Open Setup Guide]** button |
| Kitty send-text failed | Error notification: `"Kitty send-text failed: {stderr}"` |
| No active editor | Error notification: `"No active editor. Open a file first."` |
| Clipboard write failed | Non-blocking status bar warning: `"⚠ Sent to Kitty but clipboard copy failed"` |

### Status Bar Persistent Indicator

Bottom-right status bar item showing Kitty connection status:

- `$(terminal) Kitty ✓` — socket detected
- `$(warning) Kitty ✗` — no socket found

## Architecture

Single file: `src/extension.ts`

```
extension.ts
├── activate()           — Register command + status bar
├── deactivate()         — Cleanup
├── findKittySocket()    — Scan /tmp for kitty-* socket files
├── getActiveTabInfo()   — Parse `kitty @ ls` JSON → {position, title}
├── sendToKitty()        — send-text via remote control + focus window
│   └── Returns {success, tabPosition?, tabTitle?, error?}
└── sendReference()      — Alt+K command handler
    ├── Get editor state (absolutePath, line/selection)
    ├── Build reference string
    ├── Call sendToKitty()
    ├── Write to clipboard
    └── Show feedback
```

## Kitty Integration

- Socket discovery: scan `/tmp` for files matching `kitty-*`
- Send text: `kitty @ --to unix:{socket} send-text --match recent:0 "{text}"`
- Focus window: `open -a kitty`
- Tab info: `kitty @ --to unix:{socket} ls` → parse JSON to find active tab position and title

### Kitty Prerequisites

```conf
# ~/.config/kitty/kitty.conf
allow_remote_control socket-only
listen_on unix:/tmp/kitty-socket
```

## package.json Changes

- Commands: 3 → 1 (`sendReference`)
- Keybindings: 3 → 1 (`Alt+K`, when `editorTextFocus`)
- Remove Explorer context menu contribution
- Remove Explorer-related `when` clauses
- Bump version to 1.0.0 (breaking rewrite)
