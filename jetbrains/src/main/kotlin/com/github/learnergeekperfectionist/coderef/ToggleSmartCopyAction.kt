package com.github.learnergeekperfectionist.coderef

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager

class ToggleSmartCopyAction : AnAction("Toggle Code Ref Smart Copy") {
    override fun actionPerformed(event: AnActionEvent) {
        val enabled = SmartCopySettings.getInstance().toggle()
        ReferenceClipboard.copyForSmartCopyState(selectedEditor(event), enabled)
        StatusBarUpdater.updateAll()
    }

    private fun selectedEditor(event: AnActionEvent): Editor? {
        return event.getData(CommonDataKeys.EDITOR)
            ?: event.project?.let { FileEditorManager.getInstance(it).selectedTextEditor }
    }
}
