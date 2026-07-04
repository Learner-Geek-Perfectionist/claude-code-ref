package com.github.learnergeekperfectionist.coderef

import com.intellij.ide.DataManager
import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.SystemInfo
import java.awt.AWTEvent
import java.awt.event.InputEvent
import java.awt.event.KeyEvent

@Service(Service.Level.APP)
class CodeRefShortcutDispatcherInstaller : Disposable {
    private val dispatcher = object : IdeEventQueue.NonLockedEventDispatcher {
        override fun dispatch(e: AWTEvent): Boolean {
            if (e !is KeyEvent || !isToggleShortcut(e)) {
                return false
            }

            val editor = selectedEditor(e) ?: return false
            SmartCopyToggle.toggle(editor)
            e.consume()
            return true
        }
    }

    private var installed = false

    @Synchronized
    fun install() {
        if (installed) {
            return
        }

        IdeEventQueue.getInstance().addPreprocessor(dispatcher, this)
        installed = true
    }

    override fun dispose() {
        installed = false
    }

    private fun selectedEditor(event: KeyEvent): Editor? {
        val component = event.component ?: return null
        val dataContext = DataManager.getInstance().getDataContext(component)
        return CommonDataKeys.EDITOR.getData(dataContext)
    }

    companion object {
        fun getInstance(): CodeRefShortcutDispatcherInstaller =
            ApplicationManager.getApplication().getService(CodeRefShortcutDispatcherInstaller::class.java)

        internal fun isToggleShortcut(event: KeyEvent, isMac: Boolean = SystemInfo.isMac): Boolean {
            if (event.isConsumed || event.id != KeyEvent.KEY_PRESSED || event.keyCode != KeyEvent.VK_C) {
                return false
            }

            val requiredModifiers = if (isMac) {
                InputEvent.META_DOWN_MASK or InputEvent.ALT_DOWN_MASK
            } else {
                InputEvent.CTRL_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK
            }
            val disallowedModifiers = if (isMac) {
                InputEvent.CTRL_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK or InputEvent.ALT_GRAPH_DOWN_MASK
            } else {
                InputEvent.META_DOWN_MASK or InputEvent.ALT_DOWN_MASK or InputEvent.ALT_GRAPH_DOWN_MASK
            }

            return (event.modifiersEx and requiredModifiers) == requiredModifiers &&
                (event.modifiersEx and disallowedModifiers) == 0
        }
    }
}
