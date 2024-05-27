package indi.nonoas.worktools.view

import indi.nonoas.worktools.common.CommonInsets
import indi.nonoas.worktools.pojo.vo.ExecFileVo
import indi.nonoas.worktools.pojo.vo.FuncSettingVo
import javafx.event.EventHandler
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TitledPane
import javafx.scene.layout.FlowPane
import javafx.scene.layout.VBox

/**
 * 全局搜索框的返回结果面板
 * @version
 * @since 1.3.3
 * @author Nonoas
 * @date 2024/5/28
 */
class SearchResultPane private constructor() : VBox() {

    private val tpScript = TitledPane("脚本", Label("脚本"))
    private val tpFunc = TitledPane("功能", Label("脚本"))

    init {
        children.addAll(tpScript, tpFunc)
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
            val funcPane = FlowPane(CommonInsets.SPACING_1, CommonInsets.SPACING_1).apply {
                for (funcSetting in funcSettings) {
                    val button = Button(funcSetting.getFuncName())
                    button.onAction = EventHandler {
                        MainStage.instance!!.routeCenter(funcSetting.getFuncCode())
                    }
                    children.add(button)
                }
            }
            pane.tpFunc.content = funcPane
            pane.tpScript

            return pane
        }
    }
}