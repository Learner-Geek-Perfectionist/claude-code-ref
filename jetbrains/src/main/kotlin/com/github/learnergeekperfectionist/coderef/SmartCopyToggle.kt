package com.github.learnergeekperfectionist.coderef

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager

internal object SmartCopyToggle {
    fun toggle(editor: Editor?) {
        val enabled = SmartCopySettings.getInstance().toggle()
        ReferenceClipboard.copyForSmartCopyState(editor, enabled)
        StatusBarUpdater.updateAll()
    }

    fun selectedEditor(event: AnActionEvent): Editor? {
        return event.getData(CommonDataKeys.EDITOR)
            ?: event.project?.let { FileEditorManager.getInstance(it).selectedTextEditor }
    }
}
