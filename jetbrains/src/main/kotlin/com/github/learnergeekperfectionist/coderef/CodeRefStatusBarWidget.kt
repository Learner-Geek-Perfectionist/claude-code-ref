package com.github.learnergeekperfectionist.coderef

import com.intellij.icons.AllIcons
import com.intellij.openapi.wm.CustomStatusBarWidget
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Graphics
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.SwingUtilities

class CodeRefStatusBarWidget(
    private val settings: SmartCopySettings = SmartCopySettings.getInstance(),
    private val updateStatusBars: () -> Unit = StatusBarUpdater::updateAll,
) : StatusBarWidget, CustomStatusBarWidget {
    private var statusBar: StatusBar? = null
    private val component = CodeRefStatusBarComponent(settings, ::toggle)

    override fun ID(): String = CodeRefStatusBarWidgetFactory.ID

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
    }

    override fun getComponent(): JComponent = component

    override fun dispose() {
        statusBar = null
    }

    private fun toggle() {
        settings.toggle()
        component.refresh()
        statusBar?.updateWidget(ID())
        updateStatusBars()
    }
}

private class CodeRefStatusBarComponent(
    private val settings: SmartCopySettings,
    onToggle: () -> Unit,
) : JPanel(BorderLayout()) {
    private val label = JLabel().apply {
        horizontalAlignment = SwingConstants.CENTER
        iconTextGap = 6
        isOpaque = false
    }

    private val clickListener = object : MouseAdapter() {
        override fun mouseClicked(event: MouseEvent) {
            if (SwingUtilities.isLeftMouseButton(event)) {
                onToggle()
            }
        }

        override fun mouseEntered(event: MouseEvent) {
            refresh()
        }
    }

    init {
        isOpaque = false
        add(label, BorderLayout.CENTER)
        addMouseListener(clickListener)
        label.addMouseListener(clickListener)
        refresh()
    }

    fun refresh() {
        applyState(updateLayout = true)
    }

    override fun getPreferredSize(): Dimension {
        applyState(updateLayout = false)
        return super.getPreferredSize()
    }

    override fun paintComponent(graphics: Graphics) {
        applyState(updateLayout = false)
        super.paintComponent(graphics)
    }

    private fun applyState(updateLayout: Boolean) {
        val enabled = settings.enabled
        label.text = statusText(enabled)
        label.icon = statusIcon(enabled)
        label.preferredSize = widestLabelSize()

        val tooltip = statusTooltip(enabled)
        toolTipText = tooltip
        label.toolTipText = tooltip
        accessibleContext?.accessibleName = label.text
        label.accessibleContext?.accessibleName = label.text

        if (updateLayout) {
            revalidate()
            repaint()
        }
    }

    private fun widestLabelSize(): Dimension {
        val offSize = labelSize(enabled = false)
        val onSize = labelSize(enabled = true)

        return Dimension(
            maxOf(offSize.width, onSize.width),
            maxOf(offSize.height, onSize.height),
        )
    }

    private fun labelSize(enabled: Boolean): Dimension {
        return JLabel(statusText(enabled), statusIcon(enabled), SwingConstants.CENTER).apply {
            font = label.font
            iconTextGap = label.iconTextGap
        }.preferredSize
    }
}

private fun statusText(enabled: Boolean): String =
    if (enabled) "Code Ref ON" else "Code Ref OFF"

private fun statusTooltip(enabled: Boolean): String =
    if (enabled) {
        "Smart Copy is enabled. Selected editor text copies as a code reference."
    } else {
        "Smart Copy is disabled. Click to enable code-reference copying."
    }

private fun statusIcon(enabled: Boolean): Icon =
    if (enabled) AllIcons.Actions.Checked else AllIcons.Actions.Cancel
