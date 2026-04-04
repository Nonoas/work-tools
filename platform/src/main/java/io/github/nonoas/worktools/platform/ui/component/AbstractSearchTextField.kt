package io.github.nonoas.worktools.platform.ui.component

import atlantafx.base.controls.CustomTextField
import github.nonoas.jfx.flat.ui.theme.Styles
import javafx.event.EventHandler
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent

abstract class AbstractSearchTextField(
    override val mode: SearchMode,
    prompt: String = "Enter搜索，Tab切换模式",
) : CustomTextField(), SearchTextField, EventHandler<KeyEvent> {

    private var switchModeHandler: (() -> Unit)? = null

    override val view: CustomTextField
        get() = this

    init {
        promptText = prompt
        styleClass.add(Styles.ROUNDED)
        left = Label(mode.prefix).apply {
            styleClass.addAll("hint", Styles.TEXT_MUTED, Styles.TEXT_SMALL)
            tooltip = Tooltip(mode.tooltipText)
        }

        textProperty().addListener { _, _, newValue ->
            onKeywordChanged(newValue.orEmpty())
        }

        addEventHandler(KeyEvent.KEY_PRESSED) {
            if (it.code == KeyCode.TAB) {
                it.consume()
            }
        }
        addEventHandler(KeyEvent.KEY_RELEASED, this)
    }

    override fun bindModeSwitcher(onSwitchMode: () -> Unit) {
        switchModeHandler = onSwitchMode
    }

    override fun syncText(value: String) {
        val changed = text != value
        text = value
        positionCaret(text.length)
        if (!changed) {
            onKeywordChanged(value)
        }
    }

    override fun focusInput() {
        requestFocus()
        positionCaret(text.length)
    }

    override fun handle(event: KeyEvent) {
        when (event.code) {
            KeyCode.ENTER -> onEnter(event)
            KeyCode.TAB -> {
                switchModeHandler?.invoke()
                event.consume()
            }

            else -> onKeyReleased(event)
        }
    }

    protected open fun onKeywordChanged(keyword: String) {
    }

    protected open fun onKeyReleased(event: KeyEvent) {
    }

    protected abstract fun onEnter(event: KeyEvent)
}
