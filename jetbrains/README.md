# Code Ref JetBrains 插件（Android Studio / CLion）

选中代码后复制成绝对路径引用：

```text
@/path/to/file.kt#10-15
@/path/to/file.cpp#10-15
```

一份 zip，装进 Android Studio 或 CLion。

## 快捷键

| 操作 | macOS | Windows / Linux |
|---|---|---|
| 切换 Smart Copy | **`Cmd+Option+C`** | **`Ctrl+Shift+C`** |
| ON 且编辑器有选区时复制引用 | `Cmd+C` | `Ctrl+C` |

也可以点状态栏 `Code Ref OFF` / `ON`。

macOS 上 **`Cmd+Shift+C` 不是开关**。那是 Copy Paths。Mac 开关是 Option（Alt），不是 Shift。

## 使用

1. 点状态栏 `Code Ref OFF`，或按上面的切换键，变成 `ON`。
2. 在 Android Studio / CLion 的文件编辑器里选中代码。
3. `Cmd+C` / `Ctrl+C`。
4. 到别处粘贴。

只在「支持的文件编辑器 + 有选区 + Smart Copy ON」时改复制。终端、空 path 的 buffer、没选区、开关关闭，都走默认复制。

切换时如果已经有选区：ON 立刻写 `@/path#lines`，OFF 立刻写原文。

## 安装

```bash
./gradlew test
./gradlew buildPlugin
```

产物：`build/distributions/code-ref-jetbrains-<version>.zip`（当前是 `1.3.0`）。

两个 IDE 都是 Settings → Plugins → 齿轮 → **Install Plugin from Disk** → 选 zip → 重启。

插件不会出现在 Marketplace 的「捆绑插件更新」里。打开 **已安装**，看 **用户安装**，或搜 `Code Ref`。

先卸掉同 id 的旧版再装。CLion 2026.2 拒绝 `1.2.1`（`until-build=261.*`）。CLion 回滚 = 卸载。卸完后被摘掉的快捷键不一定自动回来，需要时 Settings → Keymap Reset。

编译 / 单测默认对着本机 Android Studio。不要用 CLion 当 `./gradlew test` 或 `runIde` 的 SDK。

## 快捷键冲突

插件会把切换键写进当前 keymap，并摘掉占着同一和弦的默认 action。

| OS | Keymap | 插件用的键 | 被摘掉的默认动作 |
|---|---|---|---|
| macOS | 默认 macOS、macOS System Shortcuts | `Cmd+Option+C` | Introduce Constant |
| macOS | Xcode | `Cmd+Option+C` | Commit（`CheckinProject`） |
| macOS | Eclipse | `Cmd+Option+C` | Change Signature |
| Windows / Linux | `$default` | `Ctrl+Shift+C` | Copy Paths |

还想用原来的 Introduce Constant / Commit / Change Signature / Copy Paths，到 Settings → Keymap 另绑一个键。

编辑器里有焦点时，按键先被插件吃掉。焦点在 Project 树、Commit 窗口时，全靠 keymap 里已经摘掉冲突。Xcode keymap 下如果没摘掉 Commit，在 Project 树上按 `Cmd+Option+C` 会打开 Commit。

## 支持范围

官方：Android Studio，以及本机 monolith CLion。Gateway / thinClient / frontend-split 不承诺。
