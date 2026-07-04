package com.github.learnergeekperfectionist.coderef

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.KeyboardShortcut
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.keymap.Keymap
import com.intellij.openapi.keymap.KeymapManager
import com.intellij.openapi.keymap.KeymapManagerListener
import com.intellij.openapi.keymap.ex.KeymapManagerEx
import com.intellij.openapi.util.SystemInfo
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import javax.swing.KeyStroke

@Service(Service.Level.APP)
class CodeRefKeymapShortcutInstaller : Disposable, KeymapManagerListener {
    private var installed = false

    @Synchronized
    fun install() {
        if (installed) {
            return
        }

        installInto(KeymapManager.getInstance().activeKeymap)
        KeymapManagerEx.getInstanceEx().addWeakListener(this)
        installed = true
    }

    override fun activeKeymapChanged(keymap: Keymap?) {
        keymap?.let(::installInto)
    }

    override fun dispose() {
        KeymapManagerEx.getInstanceEx().removeWeakListener(this)
        installed = false
    }

    private fun installInto(keymap: Keymap) {
        val shortcut = toggleShortcut()
        conflictingActionIds(keymap, shortcut)
            .filterNot { it == ACTION_ID }
            .forEach { keymap.removeShortcut(it, shortcut) }

        if (!keymap.getShortcuts(ACTION_ID).contains(shortcut)) {
            keymap.addShortcut(ACTION_ID, shortcut)
        }
    }

    private fun conflictingActionIds(keymap: Keymap, shortcut: KeyboardShortcut): Set<String> {
        val firstKeyStroke = shortcut.firstKeyStroke
        val candidates = linkedSetOf<String>().apply {
            addAll(keymap.getActionIdList(shortcut))
            addAll(keymap.getActionIds(firstKeyStroke).toList())
        }
        return candidates
            .filterTo(linkedSetOf()) { actionId ->
                keymap.getShortcuts(actionId)
                    .filterIsInstance<KeyboardShortcut>()
                    .any { it == shortcut }
            }
    }

    companion object {
        internal const val ACTION_ID = "CodeRef.ToggleSmartCopy"

        fun getInstance(): CodeRefKeymapShortcutInstaller =
            ApplicationManager.getApplication().getService(CodeRefKeymapShortcutInstaller::class.java)

        internal fun toggleShortcut(isMac: Boolean = SystemInfo.isMac): KeyboardShortcut {
            val modifiers = if (isMac) {
                InputEvent.META_DOWN_MASK or InputEvent.ALT_DOWN_MASK
            } else {
                InputEvent.CTRL_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK
            }
            return KeyboardShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_C, modifiers), null)
        }
    }
}
