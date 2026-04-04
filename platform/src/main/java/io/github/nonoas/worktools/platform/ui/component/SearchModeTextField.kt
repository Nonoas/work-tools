package io.github.nonoas.worktools.platform.ui.component

import javafx.application.Platform
import javafx.scene.layout.StackPane

class SearchModeTextField(
    private val fields: List<SearchTextField> = listOf(
        GlobalSearchTextField(),
        CurrentSearchTextField(),
        LlmSearchTextField(),
    ),
) : StackPane() {

    private var currentIndex = 0

    private val currentField: SearchTextField
        get() = fields[currentIndex]

    init {
        prefWidth = 240.0
        minWidth = 240.0

        fields.forEach { field ->
            field.bindModeSwitcher(::switchMode)
            field.view.prefWidthProperty().bind(widthProperty())
        }

        children.setAll(currentField.view)
    }

    private fun switchMode() {
        val currentText = currentField.view.text.orEmpty()
        currentIndex = (currentIndex + 1) % fields.size

        val nextField = currentField
        children.setAll(nextField.view)
        nextField.syncText(currentText)

        Platform.runLater {
            nextField.focusInput()
        }
    }

    fun focusInput() {
        currentField.focusInput()
    }
}
