package com.github.learnergeekperfectionist.coderef

import com.intellij.icons.AllIcons
import com.intellij.openapi.wm.CustomStatusBarWidget
import com.intellij.openapi.wm.StatusBarWidget
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JLabel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class CodeRefStatusBarWidgetTest {
    @Test
    fun `status bar component shows disabled state with circle slash icon`() {
        val settings = SmartCopySettings().apply { enabled = false }

        val widget: StatusBarWidget = CodeRefStatusBarWidget(settings)

        assertTrue(widget is CustomStatusBarWidget)
        val label = statusLabel((widget as CustomStatusBarWidget).component)
        assertEquals("Code Ref OFF", label.text)
        assertSame(AllIcons.Actions.Cancel, label.icon)
        assertEquals(
            "Smart Copy is disabled. Click to enable code-reference copying.",
            label.toolTipText,
        )
    }

    @Test
    fun `status bar component shows enabled state with check icon`() {
        val settings = SmartCopySettings().apply { enabled = true }

        val widget: StatusBarWidget = CodeRefStatusBarWidget(settings)

        assertTrue(widget is CustomStatusBarWidget)
        val label = statusLabel((widget as CustomStatusBarWidget).component)
        assertEquals("Code Ref ON", label.text)
        assertSame(AllIcons.Actions.Checked, label.icon)
        assertEquals(
            "Smart Copy is enabled. Selected editor text copies as a code reference.",
            label.toolTipText,
        )
    }

    @Test
    fun `clicking status bar component toggles setting and refreshes state`() {
        val settings = SmartCopySettings().apply { enabled = false }
        val events = mutableListOf<String>()
        val widget = CodeRefStatusBarWidget(
            settings,
            updateStatusBars = { events.add("status") },
            copySelectedTextForState = { _, enabled ->
                events.add("copy:$enabled")
                true
            },
        )
        val component = (widget as CustomStatusBarWidget).component
        val label = statusLabel(component)

        component.dispatchEvent(
            MouseEvent(
                component,
                MouseEvent.MOUSE_CLICKED,
                System.currentTimeMillis(),
                0,
                1,
                1,
                1,
                false,
                MouseEvent.BUTTON1,
            ),
        )

        assertTrue(settings.enabled)
        assertEquals("Code Ref ON", label.text)
        assertSame(AllIcons.Actions.Checked, label.icon)
        assertEquals(listOf("copy:true", "status"), events)
    }

    private fun statusLabel(component: JComponent): JLabel {
        if (component is JLabel) {
            return component
        }

        return component.components.filterIsInstance<JLabel>().single()
    }
}
