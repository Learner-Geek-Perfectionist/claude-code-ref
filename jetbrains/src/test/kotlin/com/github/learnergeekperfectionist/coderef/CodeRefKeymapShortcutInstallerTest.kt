package com.github.learnergeekperfectionist.coderef

import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import javax.swing.KeyStroke
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CodeRefKeymapShortcutInstallerTest {
    @Test
    fun `builds macOS command option C shortcut`() {
        val shortcut = CodeRefKeymapShortcutInstaller.toggleShortcut(isMac = true)

        assertEquals(
            KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.META_DOWN_MASK or InputEvent.ALT_DOWN_MASK),
            shortcut.firstKeyStroke,
        )
        assertNull(shortcut.secondKeyStroke)
    }

    @Test
    fun `builds non-mac control shift C shortcut`() {
        val shortcut = CodeRefKeymapShortcutInstaller.toggleShortcut(isMac = false)

        assertEquals(
            KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK or InputEvent.SHIFT_DOWN_MASK),
            shortcut.firstKeyStroke,
        )
        assertNull(shortcut.secondKeyStroke)
    }
}
