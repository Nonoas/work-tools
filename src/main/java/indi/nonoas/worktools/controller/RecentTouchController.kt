package indi.nonoas.worktools.controller

import indi.nonoas.worktools.common.FuncCode
import indi.nonoas.worktools.ui.FuncPaneFactory
import indi.nonoas.worktools.view.RecentTouchPane
import javafx.scene.Parent

/**
 * @author Nonoas
 * @datetime 2022/5/12 21:06
 */
@FuncCode("RecentTouch")
class RecentTouchController : FuncPaneFactory {
    override fun getRootView(): Parent {
        return RecentTouchPane.instance!!
    }
}
