# Claude Code Reference

一键复制代码引用（`@file#line` 格式）并直接发送到 [Kitty](https://sw.kovidgoyal.net/kitty/) 终端，配合 [Claude Code](https://docs.anthropic.com/en/docs/claude-code) CLI 使用。

## 功能

- **一键操作**：选中代码，按 `Alt+K`，引用自动发送到 Kitty 并聚焦终端
- **智能文件名**：文件名唯一时只用文件名，有重名时使用相对路径
- **Kitty 集成**：通过 Kitty remote control 直接发送文本，稳定可靠
- **剪贴板备份**：引用同时复制到剪贴板

### 引用格式

| 场景 | 输出 |
|------|------|
| 光标在第 42 行 | `@extension.ts#42` |
| 选中第 7-12 行 | `@extension.ts#7-12` |
| 文件名有重复 | `@src/utils/index.ts#5` |
| 工作区外的文件 | `@/full/path/to/file.ts#10` |

## 前置配置

### 1. 配置 Kitty 终端

本插件依赖 Kitty 的 remote control 功能，需要在 `~/.config/kitty/kitty.conf` 中添加以下配置：

```conf
allow_remote_control socket-only
listen_on unix:/tmp/kitty-socket
```

添加后**重启 Kitty** 使配置生效。

### 2. 安装插件

从 [GitHub Releases](https://github.com/Learner-Geek-Perfectionist/claude-code-ref/releases) 下载 `.vsix` 文件，然后安装：

```bash
code --install-extension claude-code-ref-0.1.0.vsix
```

或者从源码构建：

```bash
git clone https://github.com/Learner-Geek-Perfectionist/claude-code-ref.git
cd claude-code-ref
npm install
npm run compile
npx vsce package
code --install-extension claude-code-ref-0.1.0.vsix
```

## 使用方法

1. 在 VS Code 中打开文件
2. 将光标放在某行，或选中一段代码
3. 按 `Alt+K`
4. 引用自动发送到 Kitty 终端并聚焦

## 自定义快捷键

默认快捷键：`Alt+K`（编辑器聚焦时）

如需修改，在 `keybindings.json` 中添加：

```json
{
  "key": "your+shortcut",
  "command": "claude-code-ref.copyReference",
  "when": "editorTextFocus"
}
```

## License

MIT
