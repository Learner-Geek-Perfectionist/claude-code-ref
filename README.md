# Code Ref

把编辑器里选中的代码复制成绝对路径引用，例如：

```text
@/path/to/file.ts#10-15
@/path/to/file.cpp#10-15
```

默认关闭。打开 Smart Copy 之后，系统复制快捷键在「编辑器有选区」时写出引用，而不是原文。给 Claude Code、Codex CLI 这类认 `@/path#lines` 的工具用。

支持 **VS Code**、**Android Studio**、**CLion**。同一套契约。

## 快捷键（先看这个）

| 操作 | macOS | Windows / Linux |
|---|---|---|
| 切换 Smart Copy | **`Cmd+Option+C`**（`Cmd+Alt+C`） | **`Ctrl+Shift+C`** |
| Smart Copy ON 且有选区时复制引用 | `Cmd+C` | `Ctrl+C` |
| 点状态栏 `Code Ref OFF` / `ON` | 和快捷键等价 | 和快捷键等价 |

macOS **不要**按 `Cmd+Shift+C`。那是 IDE / 系统的 Copy Paths，插件不会拿它当开关。

有选区时切换开关会立刻改剪贴板：切到 ON 写引用，切到 OFF 写原文。没选区、开关关着、焦点不在支持的编辑器里，复制行为不变。

状态栏：

- `Code Ref ON` — `Cmd+C` / `Ctrl+C` 复制 `@/绝对路径#行号`
- `Code Ref OFF` — 普通复制

## 仓库

```text
code-ref/
  vscode/      VS Code 扩展（1.2.1）
  jetbrains/   Android Studio / CLion 插件（1.3.0）
```

VS Code 开关存在用户设置 `code-ref.smartCopy.enabled`，所有窗口共用。JetBrains 是应用级设置，所有项目窗口共用。

## 安装

### VS Code

```bash
npm run install:vscode
npm run package:vscode
code --install-extension vscode/code-ref-1.2.1.vsix --force
```

### Android Studio / CLion

```bash
npm run package:jetbrains
```

产物：

```text
jetbrains/build/distributions/code-ref-jetbrains-1.3.0.zip
```

两个 IDE 都是：**Settings → Plugins → 齿轮 → Install Plugin from Disk → 选 zip → 重启**。

不要去 Marketplace 的「捆绑插件更新」里找。从磁盘装的插件在 **已安装 → 用户安装**。搜索框输入 `Code Ref`。

已装过同一 plugin id 时，先卸载再装，否则同版本号可能被跳过。`1.2.1` 装不进 CLion 2026.2（`until-build` 只到 261）。CLion 回滚只能卸载，不能重装 1.2.1。

更细的冲突表、构建说明见 [`jetbrains/README.md`](jetbrains/README.md)。

## 环境

- VS Code 1.85+
- Android Studio 2026.1（261）与本机 CLion 2026.2（262）
- JetBrains 插件声明 `since-build=253`、`until-build=262.*`
- Windows / Linux / macOS

IntelliJ IDEA / PyCharm 262 能装，但不承诺。CLion Gateway / split 不承诺。

## 许可证

MIT
