package indi.nonoas.worktools.controller

import indi.nonoas.worktools.common.FuncCode
import indi.nonoas.worktools.ui.FuncPaneFactory
import indi.nonoas.worktools.view.launcher.ExecFilePane
import javafx.scene.Parent

/**
 * @author Nonoas
 * @datetime 2022/5/12 20:48
 */
@FuncCode("ExecFile")
class ExecFileController : FuncPaneFactory {
    override fun getRootView(): Parent {
        return ExecFilePane.instance!!
    }
}
