# Claude Code Reference

A VSCode extension that sends code references to [Kitty](https://sw.kovidgoyal.net/kitty/) terminal for use with [Claude Code](https://docs.anthropic.com/en/docs/claude-code) CLI.

Press `Alt+K` in the editor to send the current file path and line number(s) to Kitty — no copy-paste needed.

## How It Works

1. You place your cursor (or select lines) in a file
2. Press `Alt+K`
3. The extension sends a reference like `@/path/to/file.ts#42` to the active Kitty tab
4. The reference is also copied to your clipboard
5. Kitty is focused automatically

For multi-line selections, references use range format: `@/path/to/file.ts#10-25`

Multiple cursors are supported — each cursor/selection generates a separate reference.

## Requirements

- **macOS** (uses `open -a kitty` for focus)
- **[Kitty terminal](https://sw.kovidgoyal.net/kitty/)** with [remote control](https://sw.kovidgoyal.net/kitty/remote-control/) enabled
- **VSCode** 1.85+

### Enable Kitty Remote Control

Add to your `kitty.conf`:

```
allow_remote_control yes
listen_on unix:/tmp/kitty-socket
```

Then restart Kitty.

## Installation

```bash
# Clone and build
git clone https://github.com/Learner-Geek-Perfectionist/claude-code-ref.git
cd claude-code-ref
npm install
npm run compile
npm run package

# Install the .vsix file
code --install-extension claude-code-ref-1.0.0.vsix
```

## Usage

| Action | Shortcut |
|--------|----------|
| Send code reference to Kitty | `Alt+K` |

The status bar shows Kitty connection status:
- `$(terminal) Kitty ✓` — connected
- `$(warning) Kitty ✗` — socket not found

## License

MIT
