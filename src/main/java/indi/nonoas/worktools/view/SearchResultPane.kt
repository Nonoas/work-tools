package indi.nonoas.worktools.view

import indi.nonoas.worktools.common.CommonInsets
import indi.nonoas.worktools.pojo.vo.ExecFileVo
import indi.nonoas.worktools.pojo.vo.FuncSettingVo
import indi.nonoas.worktools.view.launcher.ExecFileButton
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TitledPane
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.FlowPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.util.LinkedList

/**
 * 全局搜索框的返回结果面板
 * @version
 * @since 1.3.3
 * @author Nonoas
 * @date 2024/5/28
 */
class SearchResultPane private constructor() : VBox(), EventHandler<KeyEvent> {

    private val tpScript = TitledPane("脚本", Label("脚本")).apply {
        styleClass.add("non-border")
    }
    private val tpFunc = TitledPane("功能", Label("脚本")).apply {
        styleClass.add("non-border")
    }

    companion object {
        const val SEARCH_BUTTON_SELECTED = "search-button-selected"
    }

    /**
     *  用于遍历按钮
     */
    private val buttonList = LinkedList<Button>()

    init {
        children.addAll(tpScript, tpFunc)
        padding = Insets(1.0)
    }

    private fun nextButton() {
        if (buttonList.isEmpty()) {
            return
        }
        val poll = buttonList.poll()
        poll.styleClass.remove(SEARCH_BUTTON_SELECTED)
        buttonList.offer(poll)
        buttonList.peek().styleClass.add(SEARCH_BUTTON_SELECTED)
    }

    private fun preButton() {
        if (buttonList.isEmpty()) {
            return
        }
        buttonList.peek().styleClass.remove(SEARCH_BUTTON_SELECTED)
        val last = buttonList.removeLast()
        last.styleClass.add(SEARCH_BUTTON_SELECTED)
        buttonList.push(last)
    }

    override fun handle(event: KeyEvent) {
        when (event.code) {
            KeyCode.LEFT -> {
                preButton()
                event.consume()
            }

            KeyCode.RIGHT -> {
                nextButton()
                event.consume()
            }

            KeyCode.ENTER -> {
                val peek = buttonList.peek()
                peek?.fire()
                if (peek is ExecFileButton) {
                    (scene.window as Stage).close()
                }
                event.consume()
            }

            else -> return
        }
    }

    class Builder {
        private lateinit var funcSettings: Collection<FuncSettingVo>
        private lateinit var execFiles: List<ExecFileVo>
        fun funcSettings(funcSettings: Collection<FuncSettingVo>): Builder {
            this.funcSettings = funcSettings
            return this
        }

        fun execFiles(execFiles: List<ExecFileVo>): Builder {
            this.execFiles = execFiles
            return this
        }

        fun build(): SearchResultPane {
            val pane = SearchResultPane()

            val execPane = FlowPane(CommonInsets.SPACING_1, CommonInsets.SPACING_1).apply {
                for (execVo in execFiles) {
                    val button = ExecFileButton(execVo)
                    children.add(button)
                    pane.buttonList.add(button)
                }
            }

            val funcPane = FlowPane(CommonInsets.SPACING_1, CommonInsets.SPACING_1).apply {
                for (funcSetting in funcSettings) {
                    val button = Button(funcSetting.getFuncName())
                    button.onAction = EventHandler {
                        MainStage.instance!!.routeCenter(funcSetting.getFuncCode())
                    }
                    children.add(button)
                    pane.buttonList.add(button)
                }
            }

            if (pane.buttonList.isNotEmpty()) {
                pane.buttonList.peek().styleClass.add(SEARCH_BUTTON_SELECTED)
            }

            pane.tpFunc.content = funcPane
            pane.tpScript.content = execPane

            return pane
        }
    }
}