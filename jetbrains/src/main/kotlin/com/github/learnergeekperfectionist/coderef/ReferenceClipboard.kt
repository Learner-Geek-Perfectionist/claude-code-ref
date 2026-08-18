package com.github.learnergeekperfectionist.coderef

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.util.TextRange
import java.awt.datatransfer.StringSelection

object ReferenceClipboard {
    fun isSupportedFilePath(path: String?): Boolean = !path.isNullOrBlank()

    fun canCopyFrom(
        editor: Editor?,
        requireSmartCopyEnabled: Boolean = false,
    ): Boolean {
        if (editor == null) {
            return false
        }
        if (requireSmartCopyEnabled && !SmartCopySettings.getInstance().enabled) {
            return false
        }

        return isSupportedFilePath(FileDocumentManager.getInstance().getFile(editor.document)?.path) &&
            selectedRanges(editor).isNotEmpty()
    }

    fun copyFrom(
        editor: Editor?,
        requireSmartCopyEnabled: Boolean = false,
    ): Boolean {
        if (editor == null) {
            return false
        }
        if (requireSmartCopyEnabled && !SmartCopySettings.getInstance().enabled) {
            return false
        }

        val filePath = FileDocumentManager.getInstance().getFile(editor.document)?.path
        if (!isSupportedFilePath(filePath)) {
            return false
        }
        val selections = selectedRanges(editor)
        if (selections.isEmpty()) {
            return false
        }

        CopyPasteManager.getInstance().setContents(
            StringSelection(ReferenceBuilder.build(checkNotNull(filePath), selections)),
        )
        return true
    }

    fun copyForSmartCopyState(editor: Editor?, smartCopyEnabled: Boolean): Boolean {
        return if (smartCopyEnabled) {
            copyFrom(editor)
        } else {
            copySelectedTextFrom(editor)
        }
    }

    fun copySelectedTextFrom(editor: Editor?): Boolean {
        if (editor == null) {
            return false
        }

        val selectedText = editor.caretModel.allCarets
            .filter { it.hasSelection() }
            .map { caret ->
                editor.document.getText(
                    TextRange(caret.selectionStart, caret.selectionEnd),
                )
            }
            .joinToString("\n")
        if (selectedText.isEmpty()) {
            return false
        }

        CopyPasteManager.getInstance().setContents(StringSelection(selectedText))
        return true
    }

    private fun selectedRanges(editor: Editor): List<ReferenceSelection> {
        return editor.caretModel.allCarets
            .filter { it.hasSelection() }
            .map { caret ->
                val startPosition = editor.offsetToLogicalPosition(caret.selectionStart)
                val endPosition = editor.offsetToLogicalPosition(caret.selectionEnd)

                ReferenceSelection(
                    startLine = startPosition.line,
                    endLine = endPosition.line,
                    endColumn = endPosition.column,
                )
            }
    }
}
