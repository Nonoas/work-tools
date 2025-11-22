package indi.nonoas.worktools.launcher

import indi.nonoas.worktools.common.FuncCode
import indi.nonoas.worktools.ui.FuncPaneFactory
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