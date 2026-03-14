# Claude Code Reference

一个 VSCode 扩展，按 `Alt+K` 即可将代码引用（绝对路径 + 行号）发送到 [Kitty](https://sw.kovidgoyal.net/kitty/) 终端，配合 [Claude Code](https://docs.anthropic.com/en/docs/claude-code) CLI 使用。

## 工作原理

1. 在编辑器中将光标放到目标行（或选中多行）
2. 按 `Alt+K`
3. 扩展将引用（如 `@/path/to/file.ts#42`）发送到当前 Kitty 活动标签页
4. 引用同时复制到剪贴板
5. 自动聚焦 Kitty 窗口

多行选区使用范围格式：`@/path/to/file.ts#10-25`

支持多光标 — 每个光标/选区生成一条独立引用。

## 环境要求

- **macOS**（使用 `open -a kitty` 聚焦窗口）
- **[Kitty 终端](https://sw.kovidgoyal.net/kitty/)**，需开启[远程控制](https://sw.kovidgoyal.net/kitty/remote-control/)
- **VSCode** 1.85+

### 开启 Kitty 远程控制

在 `kitty.conf` 中添加：

```
allow_remote_control yes
listen_on unix:/tmp/kitty-socket
```

然后重启 Kitty。

## 安装

```bash
# 克隆并构建
git clone https://github.com/Learner-Geek-Perfectionist/claude-code-ref.git
cd claude-code-ref
npm install
npm run compile
npm run package

# 安装 .vsix 文件
code --install-extension claude-code-ref-1.0.0.vsix
```

## 使用

| 操作 | 快捷键 |
|------|--------|
| 发送代码引用到 Kitty | `Alt+K` |

状态栏显示 Kitty 连接状态：
- `Kitty ✓` — 已连接
- `Kitty ✗` — 未找到 socket

## 许可证

MIT
