package indi.nonoas.worktools.platform.ui.component

import atlantafx.base.controls.CustomTextField
import github.nonoas.jfx.flat.ui.theme.Styles
import indi.nonoas.worktools.platform.global.message.MessageBus
import javafx.event.EventHandler
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.Tooltip
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent

/**
 * @author huangshengsheng
 * @date 2026/1/8 17:45
 */
class SearchTextField : CustomTextField(), EventHandler<KeyEvent> {

    init {
        promptText = "输入关键字，回车搜索"
        styleClass.add(Styles.ROUNDED)
        left = Label("Qry_>").apply {
            styleClass.addAll("hint", Styles.TEXT_MUTED, Styles.TEXT_SMALL)
            tooltip = Tooltip("搜索模式")
        }

        onTextChanged {
            MessageBus.getPublisher(SearchListener.TOPIC).onTextChange(it)
        }

        addEventHandler(KeyEvent.KEY_PRESSED, this)
    }

    /**
     * 文本变化监听器
     */
    private fun TextField.onTextChanged(action: (String) -> Unit) {
        textProperty().addListener { _, _, newValue -> action(newValue) }
    }

    override fun handle(p0: KeyEvent) {
        if (KeyCode.ENTER == p0.code) {
            MessageBus.getPublisher(SearchListener.TOPIC).onEntered(p0)
        } else {
            MessageBus.getPublisher(SearchListener.TOPIC).onKeyPressed(p0)
        }
    }

}