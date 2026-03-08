# Claude Code Reference

VS Code extension that copies file references in `@file#line` format and sends them directly to [Kitty](https://sw.kovidgoyal.net/kitty/) terminal for use with [Claude Code](https://docs.anthropic.com/en/docs/claude-code) CLI.

一键复制代码引用并发送到 Kitty 终端，配合 Claude Code CLI 使用。

## Features

- **One-key workflow**: Select code, press `Alt+K` — reference is sent to Kitty and the terminal is focused
- **Smart file naming**: Uses basename when unique in workspace, falls back to relative path when duplicates exist
- **Kitty integration**: Sends text via Kitty remote control (no flaky keystroke simulation)
- **Clipboard backup**: Reference is also copied to clipboard

### Reference format

| Scenario | Output |
|----------|--------|
| Cursor on line 42 | `@extension.ts#42` |
| Selection lines 7-12 | `@extension.ts#7-12` |
| Duplicate filename | `@src/utils/index.ts#5` |
| Outside workspace | `@/full/path/to/file.ts#10` |

## Prerequisites

**Kitty terminal** with remote control enabled. Add to your `~/.config/kitty/kitty.conf`:

```conf
allow_remote_control socket-only
listen_on unix:/tmp/kitty-socket
```

Then restart Kitty.

## Install

```bash
# Build from source
git clone https://github.com/Learner-Geek-Perfectionist/claude-code-ref.git
cd claude-code-ref
npm install
npm run compile
npx vsce package

# Install the .vsix
code --install-extension claude-code-ref-0.1.0.vsix
```

## Usage

1. Open a file in VS Code
2. Place cursor on a line or select a range
3. Press `Alt+K`
4. The reference is sent to Kitty and the terminal is focused

## Keybinding

Default: `Alt+K` (when editor has focus)

To customize, add to your `keybindings.json`:

```json
{
  "key": "your+shortcut",
  "command": "claude-code-ref.copyReference",
  "when": "editorTextFocus"
}
```

## License

MIT
