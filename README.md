# Code Ref

一个代码引用复制工具，支持 VS Code 和 Android Studio / JetBrains IDE。开启 Smart Copy 后，可以直接用系统复制快捷键把选中的代码复制为代码引用（绝对路径 + 行号）。macOS 使用 `Cmd+C`，Windows / Linux 使用 `Ctrl+C`。适合配合 Claude Code、Codex CLI 或其他支持文件引用的 AI 编程工具使用。

## 目录结构

```text
code-ref/
  vscode/      VS Code 插件工程，含中文 README
  jetbrains/   Android Studio / JetBrains 插件工程，含中文 README
```

## 工作原理

1. 点击状态栏的 `Code Ref OFF` 或按开关快捷键，开启 Smart Copy
2. 在编辑器中选中一段代码
3. 按系统复制快捷键：
   - macOS: `Cmd+C`
   - Windows / Linux: `Ctrl+C`
4. 扩展将引用（如 `@/path/to/file.ts#42-45`）复制到剪贴板
5. 在目标位置正常粘贴：
   - macOS: `Cmd+V`
   - Windows / Linux: `Ctrl+V`

Smart Copy 只在编辑器有选区时覆盖复制行为。未选中文本、开关关闭、或焦点不在支持的 IDE 编辑器中时，系统和 IDE 的默认复制行为不受影响。

点击状态栏 `Code Ref ON/OFF` 或按开关快捷键时，如果当前编辑器已有选区，Code Ref 会按切换后的状态立即写入剪贴板：切到 ON 复制代码引用，切到 OFF 复制选中的原文。

VS Code 版的 Smart Copy 开关保存为全局用户设置 `code-ref.smartCopy.enabled`，多个 VS Code 窗口会使用同一个开关状态。JetBrains 版使用应用级设置，多个项目窗口共享同一个开关状态。

多行选区使用范围格式：`@/path/to/file.ts#10-25`

支持多个选区 — 每个选区生成一条独立引用。

## 环境要求

- **VS Code** 1.85+
- **Android Studio / JetBrains 2025.3-2026.1 系列 IDE**
- **Windows / Linux / macOS** 均支持复制到剪贴板

## VS Code 安装

```bash
# 克隆并构建
git clone <repo-url> code-ref
cd code-ref
npm run install:vscode
npm run package:vscode

# 安装 .vsix 文件
code --install-extension vscode/code-ref-1.2.1.vsix
```

## Android Studio 安装

```bash
npm run package:jetbrains
```

生成的插件包位于：

```text
jetbrains/build/distributions/code-ref-jetbrains-1.2.1.zip
```

在 Android Studio 中打开设置里的插件页面，选择“从磁盘安装插件”，选中这个 `.zip` 文件安装，然后重启 IDE。

## 使用

| 操作 | 快捷键 |
|------|--------|
| 开关 Smart Copy，若有选区则按切换后的状态复制 | 点击状态栏 `Code Ref ON/OFF`；macOS `Cmd+Alt+C` / Windows、Linux `Ctrl+Shift+C` |
| macOS 复制代码引用 | `Cmd+C` |
| Windows / Linux 复制代码引用 | `Ctrl+C` |

Android Studio / JetBrains IDE 默认把 macOS `Cmd+Alt+C` 用作 Extract Constant。Code Ref JetBrains 插件会在 macOS 接管 `Cmd+Alt+C`，在 Windows / Linux 使用 `Ctrl+Shift+C` 切换 Smart Copy，因此不会再触发 `Cannot perform refactoring without an expression`。如果仍需要 Extract Constant，可在 Settings → Keymap 中给 Extract Constant 重新绑定一个快捷键。

状态栏显示 Smart Copy 状态：
- `Code Ref ON` — 选中文本后复制快捷键会复制代码引用
- `Code Ref OFF` — 使用 IDE 默认复制行为

Release 会同时提供：
- VS Code: `vscode/code-ref-<version>.vsix`
- Android Studio / JetBrains: `jetbrains/build/distributions/code-ref-jetbrains-<version>.zip`

## 许可证

MIT
