package io.github.nonoas.worktools.platform.ui.component

import atlantafx.base.controls.CustomTextField
import github.nonoas.jfx.flat.ui.theme.Styles
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.ContextMenu
import javafx.scene.control.Label
import javafx.scene.control.MenuItem
import javafx.scene.control.Tooltip
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent

abstract class AbstractSearchTextField(
    override val mode: SearchMode,
    prompt: String = "Enter搜索，Tab切换模式",
) : CustomTextField(), SearchTextField, EventHandler<KeyEvent> {

    private var switchModeHandler: (() -> Unit)? = null
    private var switchToSpecificModeHandler: ((SearchMode) -> Unit)? = null
    private lateinit var modeLabel: Label

    override val view: CustomTextField
        get() = this

    init {
        promptText = prompt
        styleClass.add(Styles.ROUNDED)
        modeLabel = Label(mode.prefix).apply {
            styleClass.addAll("hint", Styles.TEXT_MUTED, Styles.TEXT_SMALL)
            tooltip = Tooltip(mode.tooltipText)
            style = "-fx-cursor: hand;"

            val contextMenu = ContextMenu().apply {
                items.addAll(
                    MenuItem(SearchMode.GLOBAL.tooltipText).apply {
                        setOnAction {
                            switchToSpecificModeHandler?.invoke(SearchMode.GLOBAL)
                        }
                    },
                    MenuItem(SearchMode.CURRENT.tooltipText).apply {
                        setOnAction {
                            switchToSpecificModeHandler?.invoke(SearchMode.CURRENT)
                        }
                    },
                    MenuItem(SearchMode.LLM.tooltipText).apply {
                        setOnAction {
                            switchToSpecificModeHandler?.invoke(SearchMode.LLM)
                        }
                    }
                )
            }

            setOnMouseClicked { event ->
                if (event.button == javafx.scene.input.MouseButton.PRIMARY) {
                    contextMenu.show(this, event.screenX, event.screenY)
                }
            }
        }

        left = modeLabel

        textProperty().addListener { _, _, newValue ->
            onKeywordChanged(newValue.orEmpty())
        }

        setOnAction {
            onEnter(it)
        }

        addEventHandler(KeyEvent.KEY_RELEASED, this)
    }

    override fun bindModeSwitcher(onSwitchMode: () -> Unit) {
        switchModeHandler = onSwitchMode
    }

    override fun bindModeSwitcher(onSwitchMode: (SearchMode) -> Unit) {
        switchToSpecificModeHandler = onSwitchMode
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

    override fun updateMode(newMode: SearchMode) {
        modeLabel.text = newMode.prefix
        modeLabel.tooltip = Tooltip(newMode.tooltipText)
    }

    override fun handle(event: KeyEvent) {
        when (event.code) {
            KeyCode.TAB -> {
                switchModeHandler?.invoke()
                event.consume()
            }

            KeyCode.DIGIT1, KeyCode.NUMPAD1 -> {
                if (event.isControlDown) {
                    switchToSpecificModeHandler?.invoke(SearchMode.GLOBAL)
                    event.consume()
                } else {
                    onKeyReleased(event)
                }
            }

            KeyCode.DIGIT2, KeyCode.NUMPAD2 -> {
                if (event.isControlDown) {
                    switchToSpecificModeHandler?.invoke(SearchMode.CURRENT)
                    event.consume()
                } else {
                    onKeyReleased(event)
                }
            }

            KeyCode.DIGIT3, KeyCode.NUMPAD3 -> {
                if (event.isControlDown) {
                    switchToSpecificModeHandler?.invoke(SearchMode.LLM)
                    event.consume()
                } else {
                    onKeyReleased(event)
                }
            }

            else -> onKeyReleased(event)
        }
    }

    protected open fun onKeywordChanged(keyword: String) {
    }

    protected open fun onKeyReleased(event: KeyEvent) {
    }

    protected abstract fun onEnter(event: ActionEvent)
}
