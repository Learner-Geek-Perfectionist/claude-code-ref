package com.github.learnergeekperfectionist.coderef

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler

class CopyReferenceEditorActionHandler(
    private val delegate: EditorActionHandler,
) : EditorActionHandler() {
    @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
    override fun isEnabled(editor: Editor, dataContext: DataContext): Boolean {
        return canCopyReference(editor) || delegate.isEnabled(editor, dataContext)
    }

    override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
        if (!ReferenceClipboard.copyFrom(editor, requireSmartCopyEnabled = true)) {
            delegate.execute(editor, caret, dataContext)
        }
    }

    private fun canCopyReference(editor: Editor): Boolean {
        return ReferenceClipboard.canCopyFrom(editor, requireSmartCopyEnabled = true)
    }
}
