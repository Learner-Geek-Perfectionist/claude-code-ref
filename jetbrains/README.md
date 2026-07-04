# Code Ref JetBrains 插件

Code Ref 可以把 Android Studio / JetBrains IDE 编辑器里选中的代码复制成绝对路径代码引用，例如：

```text
@/path/to/file.kt#10-15
```

开启 Smart Copy 后，可以直接用系统复制快捷键生成引用。

## 使用方式

1. 点击状态栏的 `Code Ref OFF` 或按 `Cmd+Alt+C` / `Ctrl+Alt+C`，开启 Smart Copy。
2. 在 Android Studio 或 JetBrains IDE 编辑器中选中代码。
3. 按系统复制快捷键：
   - macOS: `Cmd+C`
   - Windows / Linux: `Ctrl+C`
4. 在目标位置正常粘贴。

Smart Copy 只在 IDE 编辑器有选区时覆盖复制行为。开关关闭、没有选中文本、或焦点不在支持的 IDE 编辑器中时，默认复制行为不受影响。

点击状态栏 `Code Ref ON/OFF` 或按 `Cmd+Alt+C` / `Ctrl+Alt+C` 时，如果当前编辑器已有选区，Code Ref 会按切换后的状态立即写入剪贴板：切到 ON 复制代码引用，切到 OFF 复制选中的原文。

## 构建

```bash
./gradlew test
./gradlew buildPlugin
```

打包产物会生成在：

```text
build/distributions/code-ref-jetbrains-<version>.zip
```

## 安装

在 Android Studio 中打开设置里的插件页面，选择“从磁盘安装插件”，选中生成的 `.zip` 文件安装，然后重启 IDE。
