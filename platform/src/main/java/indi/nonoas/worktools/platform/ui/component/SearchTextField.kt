package indi.nonoas.worktools.platform.ui.component

import atlantafx.base.controls.CustomTextField
import github.nonoas.jfx.flat.ui.theme.Styles
import indi.nonoas.worktools.platform.global.message.MessageBus
import indi.nonoas.worktools.platform.global.message.MsgBusManager
import javafx.event.EventHandler
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.Tooltip
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * @author huangshengsheng
 * @date 2026/1/8 17:45
 */
class SearchTextField : CustomTextField(), EventHandler<KeyEvent> {

    private val logger: Logger = LogManager.getLogger(SearchTextField::class.java)

    private var messageBus: MessageBus = MsgBusManager.getGlobalBus()

        private var leftGlobal = Label(" Qry_>").apply {
        styleClass.addAll("hint", Styles.TEXT_MUTED, Styles.TEXT_SMALL)
        tooltip = Tooltip("全局模式")
    }


    private var leftCurr = Label(" Cur_>").apply {
        styleClass.addAll("hint", Styles.TEXT_MUTED, Styles.TEXT_SMALL)
        tooltip = Tooltip("当前模式")
    }

    /**
     * 搜索模式
     */
    private var mode: MODE = MODE.GLOBAL

    init {
        promptText = "Enter搜索，Tab切换模式"
        styleClass.add(Styles.ROUNDED)
        left = leftGlobal

        onTextChanged {
            messageBus.getPublisher(SearchListener.TOPIC).onTextChange(it)
        }

        addEventHandler(KeyEvent.KEY_PRESSED, EventHandler {
            if (it.code == KeyCode.TAB) {
                it.consume()
            }
        })
        addEventHandler(KeyEvent.KEY_RELEASED, this)
    }

    /**
     * 文本变化监听器
     */
    private fun TextField.onTextChanged(action: (String) -> Unit) {
        textProperty().addListener { _, _, newValue -> action(newValue) }
    }

    override fun handle(p0: KeyEvent) {
        when (p0.code) {
            KeyCode.ENTER -> {
                messageBus.getPublisher(SearchListener.TOPIC).onEntered(p0)
            }

            KeyCode.TAB -> {
                mode = if (mode == MODE.GLOBAL) MODE.CURRENT else MODE.GLOBAL
                // 根据模式切换消息总线
                messageBus = if (mode == MODE.CURRENT) {
                    left = leftCurr
                    MsgBusManager.getCurrentBus()
                } else {
                    left = leftGlobal
                    MsgBusManager.getGlobalBus()
                }
                // 消费事件，避免输入框丢失焦点
                p0.consume()
                logger.debug("切换模式为{}", mode)
            }

            else -> {
                messageBus.getPublisher(SearchListener.TOPIC).onKeyPressed(p0)
            }
        }
    }

    enum class MODE {
        GLOBAL, CURRENT
    }

}