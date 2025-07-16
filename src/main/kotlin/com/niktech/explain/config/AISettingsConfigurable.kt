package com.niktech.explain.config

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.JLabel
import java.awt.BorderLayout

class AISettingsConfigurable : Configurable {
    private val apiKeyField = JTextField(30)
    private var panel: JPanel? = null

    override fun getDisplayName(): String = "AI Client Settings"

    override fun createComponent(): JComponent {
        panel = JPanel(BorderLayout())
        panel = JPanel()
        panel!!.layout = BorderLayout()
        val inputPanel = JPanel()
        inputPanel.layout = BorderLayout()
        inputPanel.add(JLabel("OpenAI API Key:"), BorderLayout.WEST)
        inputPanel.add(apiKeyField, BorderLayout.CENTER)
        panel!!.add(inputPanel, BorderLayout.NORTH)
        return panel!!
    }

    override fun isModified(): Boolean {
        return apiKeyField.text != AISettings.getInstance().state.apiKey
    }

    override fun apply() {
        AISettings.getInstance().state.apiKey = apiKeyField.text
    }

    override fun reset() {
        apiKeyField.text = AISettings.getInstance().state.apiKey
    }
}