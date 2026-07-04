package com.github.learnergeekperfectionist.coderef

import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import javax.swing.JPanel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CodeRefShortcutDispatcherInstallerTest {
    @Test
    fun `matches macOS command option C`() {
        val event = keyEvent(InputEvent.META_DOWN_MASK or InputEvent.ALT_DOWN_MASK)

        assertTrue(CodeRefShortcutDispatcherInstaller.isToggleShortcut(event, isMac = true))
    }

    @Test
    fun `matches non-mac control shift C`() {
        val event = keyEvent(InputEvent.CTRL_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK)

        assertTrue(CodeRefShortcutDispatcherInstaller.isToggleShortcut(event, isMac = false))
    }

    @Test
    fun `does not match shifted shortcut variants`() {
        val macEvent = keyEvent(
            InputEvent.META_DOWN_MASK or InputEvent.ALT_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK,
        )
        val nonMacEvent = keyEvent(
            InputEvent.CTRL_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK or InputEvent.ALT_DOWN_MASK,
        )

        assertFalse(CodeRefShortcutDispatcherInstaller.isToggleShortcut(macEvent, isMac = true))
        assertFalse(CodeRefShortcutDispatcherInstaller.isToggleShortcut(nonMacEvent, isMac = false))
    }

    @Test
    fun `does not match key release events`() {
        val event = KeyEvent(
            JPanel(),
            KeyEvent.KEY_RELEASED,
            0L,
            InputEvent.META_DOWN_MASK or InputEvent.ALT_DOWN_MASK,
            KeyEvent.VK_C,
            'C',
        )

        assertFalse(CodeRefShortcutDispatcherInstaller.isToggleShortcut(event, isMac = true))
    }

    private fun keyEvent(modifiers: Int): KeyEvent {
        return KeyEvent(
            JPanel(),
            KeyEvent.KEY_PRESSED,
            0L,
            modifiers,
            KeyEvent.VK_C,
            'C',
        )
    }
}
