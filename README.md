# Claude Code Reference

一键发送代码引用（`@/absolute/path#line` 格式）到 [Kitty](https://sw.kovidgoyal.net/kitty/) 终端，配合 [Claude Code](https://docs.anthropic.com/en/docs/claude-code) CLI 使用。

## 功能

- **一键操作**：选中代码，按 `Alt+K`，引用自动发送到 Kitty 并聚焦终端
- **绝对路径**：始终使用绝对路径，适用于 CLI 与 VSCode 工作目录不同的场景
- **多光标支持**：同文件中多个选区一次性生成多个引用
- **Kitty 集成**：通过 Kitty remote control 直接发送文本，显示目标 tab 信息
- **连接状态**：状态栏实时显示 Kitty 连接状态
- **剪贴板备份**：引用同时复制到剪贴板

### 引用格式

| 场景 | 输出 |
|------|------|
| 光标在第 42 行 | `@/Users/me/project/extension.ts#42` |
| 选中第 7-12 行 | `@/Users/me/project/extension.ts#7-12` |
| 多光标（两处） | `@/path/file.ts#5 @/path/file.ts#20-25` |

### 用户反馈

| 场景 | 反馈 |
|------|------|
| 发送成功 | 状态栏：`✓ Sent @/path/file#42 → tab #2 "Claude Code"` |
| 未找到 Kitty | 错误通知 + **[Open Setup Guide]** 按钮 |
| 发送失败 | 错误通知，附带诊断信息 |

## 前置配置

### 1. 配置 Kitty 终端

本插件依赖 Kitty 的 remote control 功能，需要在 `~/.config/kitty/kitty.conf` 中添加：

```conf
allow_remote_control socket-only
listen_on unix:/tmp/kitty-socket
```

添加后**重启 Kitty** 使配置生效。

### 2. 安装插件

下载 `.vsix` 文件后安装：

```bash
code --install-extension claude-code-ref-1.0.0.vsix
```

或从源码构建：

```bash
git clone https://github.com/Learner-Geek-Perfectionist/claude-code-ref.git
cd claude-code-ref
npm install
npm run compile
npx vsce package
code --install-extension claude-code-ref-1.0.0.vsix
```

## 使用方法

1. 在 VSCode 中打开文件
2. 将光标放在某行，或选中一段代码
3. 按 `Alt+K`
4. 引用自动发送到 Kitty 终端并聚焦

状态栏右下角显示 Kitty 连接状态：`Kitty ✓` 或 `Kitty ✗`。

## 自定义快捷键

默认快捷键：`Alt+K`（编辑器聚焦时）

如需修改，在 `keybindings.json` 中添加：

```json
{
  "key": "your+shortcut",
  "command": "claude-code-ref.sendReference",
  "when": "editorTextFocus"
}
```

## License

MIT
