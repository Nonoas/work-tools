package indi.nonoas.worktools.controller

import indi.nonoas.worktools.common.FuncCode
import indi.nonoas.worktools.ui.BaseParentController
import indi.nonoas.worktools.view.launcher.ScriptLaunchPane
import javafx.scene.Parent

/**
 * @author Nonoas
 * @datetime 2022/5/12 20:48
 */
@FuncCode("batRunner")
class LcptLaunchController : BaseParentController() {
    override fun getRootView(): Parent {
        return ScriptLaunchPane.instance!!
    }
}
