# Code Ref JetBrains 插件（Android Studio / CLion）

Code Ref 可以把 Android Studio / CLion 编辑器里选中的代码复制成绝对路径代码引用，例如：

```text
@/path/to/file.kt#10-15
@/path/to/file.cpp#10-15
```

开启 Smart Copy 后，可以直接用系统复制快捷键生成引用。

## 使用方式

1. 点击状态栏的 `Code Ref OFF`，或按 macOS `Cmd+Alt+C` / Windows、Linux `Ctrl+Shift+C`，开启 Smart Copy。
2. 在 Android Studio 或 CLion 编辑器中选中代码。
3. 按系统复制快捷键：
   - macOS: `Cmd+C`
   - Windows / Linux: `Ctrl+C`
4. 在目标位置正常粘贴。

Smart Copy 只在支持的 IDE 编辑器有选区时覆盖复制行为。开关关闭、没有选中文本、或焦点不在支持的 IDE 编辑器中时，默认复制行为不受影响。

点击状态栏 `Code Ref ON/OFF` 或按 macOS `Cmd+Alt+C` / Windows、Linux `Ctrl+Shift+C` 时，如果当前编辑器已有选区，Code Ref 会按切换后的状态立即写入剪贴板：切到 ON 复制代码引用，切到 OFF 复制选中的原文。

## 快捷键冲突

Code Ref 会接管下表中的快捷键，并从当前 keymap 摘掉冲突的默认 action。Android Studio 与 CLion 的默认 macOS keymap 都把 `Cmd+Alt+C` 绑在 Introduce Constant（`IntroduceConstant`）上。

| OS | Keymap | Code Ref 接管的键 | 被摘掉的默认 action | 用户若仍需要该 action |
|---|---|---|---|---|
| macOS | 默认 macOS，以及 macOS System Shortcuts | Cmd+Alt+C | Introduce Constant (`IntroduceConstant`) | Settings → Keymap 换键 |
| macOS | Xcode | Cmd+Alt+C | Commit / `CheckinProject` | 给 CheckinProject 换键 |
| macOS | Eclipse | Cmd+Alt+C | Change Signature（Introduce Constant 已被该 keymap 解绑） | 给 ChangeSignature 换键 |
| Windows / Linux | $default | Ctrl+Shift+C | Copy Paths (`CopyPaths`) | 给 Copy Paths 换键 |

编辑器有焦点时，dispatcher 会在事件带有 Editor 时消费切换快捷键；Project 视图、Commit 工具窗口等没有 Editor 时，只能靠 keymap installer。Xcode keymap 下若 installer 尚未摘掉 `CheckinProject`，从 Project 树按 `Cmd+Alt+C` 会打开 Commit。

## 构建

```bash
./gradlew test
./gradlew buildPlugin
```

默认编译 / 单测 SDK 是 Android Studio（`platformLocalPath`）。CLion 只通过「从磁盘安装」加载，不要把 CLion 当作 Gradle 验证目标。

打包产物会生成在：

```text
build/distributions/code-ref-jetbrains-<version>.zip
```

## 安装

同一份 zip 分别装进两个 IDE：

- **Android Studio：** Settings → Plugins → 齿轮 → Install Plugin from Disk → 选 zip → 重启。
- **CLion：** Settings → Plugins → 齿轮 → Install Plugin from Disk → 选 zip → 重启。

若已安装同一 plugin id 的旧版本或快照，先卸载再装。安装器对相同版本号可能直接跳过。

`code-ref-jetbrains-1.2.1.zip` 不能装进 CLion 2026.2（`until-build=261.*`）。CLion 回滚是卸载，不是重装 1.2.1。

卸载后，被摘掉的 keymap 快捷键不一定会自动恢复；需要时到 Settings → Keymap Reset。

## 官方支持

官方支持 Android Studio 与本机 monolith CLion。Gateway / thinClient / frontend-split 不承诺。
