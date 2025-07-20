package com.niktech.explain

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.psi.JavaRecursiveElementWalkingVisitor
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.util.PsiTreeUtil
import com.niktech.explain.ai.AIClient
import com.niktech.explain.config.AISettings
import com.niktech.explain.data.MethodInfo
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*

class ExplainMethodAction : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return
        val project = event.getData(CommonDataKeys.PROJECT) ?: return
        val psiFile = event.getData(CommonDataKeys.PSI_FILE) ?: return

        // Check if apiKey is set
        val apiKey = AISettings.getInstance().state.apiKey
        if (apiKey.isBlank()) {
            val settingsLink = JLabel("<html><a href='#'>Set API Key in Plugin Settings</a></html>")
            settingsLink.cursor = java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR)
            settingsLink.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    // Open plugin settings page
                    com.intellij.openapi.options.ShowSettingsUtil.getInstance()
                        .showSettingsDialog(project, "Explain Plugin") // Adjust display name as needed
                }
            })
            val panel = JPanel(BorderLayout()).apply {
                add(JLabel("API Key is not set."), BorderLayout.NORTH)
                add(settingsLink, BorderLayout.CENTER)
            }
            JBPopupFactory.getInstance()
                .createComponentPopupBuilder(panel, null)
                .setTitle("Missing API Key")
                .setResizable(false)
                .setMovable(true)
                .setRequestFocus(true)
                .createPopup()
                .showInBestPositionFor(editor)
            return
        }
        val offset = editor.caretModel.offset
        val element = psiFile.findElementAt(offset) ?: return
        val method = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java) ?: return
        val calledMethods = mutableListOf<PsiMethod>()

        // Collect called methods (read action)
        ApplicationManager.getApplication().runReadAction {
            method.accept(object : JavaRecursiveElementWalkingVisitor() {
                override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
                    val resolved = expression.resolveMethod()
                    if (resolved != null) {
                        calledMethods.add(resolved)
                    }
                    super.visitMethodCallExpression(expression)
                }
            })
        }

        // Prepare UI components for popup
        val closeButton = JButton("\u2716").apply {
            isFocusable = false
            isBorderPainted = false
            isContentAreaFilled = false
            toolTipText = "Close"
            preferredSize = Dimension(24, 24)
        }
        val headerPanel = JPanel(BorderLayout()).apply {
            background = UIManager.getColor("Panel.background")
            border = BorderFactory.createEmptyBorder(4, 8, 4, 4)
            val titleLabel = JLabel("Explanation of method: ${method.name}")
            add(titleLabel, BorderLayout.WEST)
            add(closeButton, BorderLayout.EAST)
        }
        var initialClick: Point? = null
        headerPanel.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                initialClick = e.point
            }
        })
        headerPanel.addMouseMotionListener(object : MouseAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                val popupWindow = SwingUtilities.getWindowAncestor(headerPanel)
                if (popupWindow != null && initialClick != null) {
                    val location = popupWindow.location
                    val x = location.x + e.x - initialClick!!.x
                    val y = location.y + e.y - initialClick!!.y
                    popupWindow.setLocation(x, y)
                }
            }
        })

        val explanationArea = JTextArea("Loading explanation...").apply {
            lineWrap = true
            wrapStyleWord = true
            isEditable = false
            rows = 20
            columns = 60
        }
        val panel = JPanel(BorderLayout()).apply {
            add(headerPanel, BorderLayout.NORTH)
            add(JScrollPane(explanationArea), BorderLayout.CENTER)
        }

        val popup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(panel, null)
            .setTitle(null)
            .setResizable(true)
            .setMovable(true)
            .setRequestFocus(true)
            .setCancelOnClickOutside(false)
            .setCancelOnOtherWindowOpen(false)
            .setCancelKeyEnabled(false)
            .createPopup()

        closeButton.addActionListener { popup.closeOk(null) }
        popup.showInBestPositionFor(editor)

        // Fetch method info and explanation in background
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                val methodInfo = ApplicationManager.getApplication().runReadAction<MethodInfo> {
                    val methodText = method.text
                    val relatedMethodSources = calledMethods
                        .filter { it.containingFile?.virtualFile != method.containingFile?.virtualFile }
                        .distinct()
                        .joinToString("\n\n") { it.text }
                    val clazz = method.containingClass
                    val fieldInfo = clazz?.allFields?.joinToString("\n") { "${it.type.presentableText} ${it.name}" }
                    val classAnnotations = clazz?.annotations?.joinToString("\n") { "@${it.qualifiedName}" }
                    MethodInfo(methodText, relatedMethodSources, fieldInfo, classAnnotations, method.name)
                }
                val explanation = AIClient.explainCode(
                    methodInfo.methodText,
                    methodInfo.relatedMethodSources,
                    methodInfo.fieldInfo,
                    methodInfo.classAnnotations
                )
                ApplicationManager.getApplication().invokeLater {
                    explanationArea.text = explanation
                }
            } catch (e: Exception) {
                ApplicationManager.getApplication().invokeLater {
                    explanationArea.text = "Error: ${e.message}"
                }
            }
        }
    }
}