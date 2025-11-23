package indi.nonoas.worktools.launcher

import indi.nonoas.worktools.platform.ui.FuncPaneFactory
import javafx.scene.Parent

/**
 * @author Nonoas
 * @datetime 2022/5/12 20:48
 */
class ExecFileController : FuncPaneFactory {
    override fun getRootView(): Parent {
        return ExecFilePane.instance!!
    }

    override fun getCode(): String {
        return "ExecFile"
    }
}