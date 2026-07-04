package com.github.learnergeekperfectionist.coderef

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

class CodeRefStartupActivity : StartupActivity.DumbAware {
    override fun runActivity(project: Project) {
        CodeRefCopyHandlerInstaller.getInstance().install()
        CodeRefShortcutDispatcherInstaller.getInstance().install()
        CodeRefKeymapShortcutInstaller.getInstance().install()
    }
}
