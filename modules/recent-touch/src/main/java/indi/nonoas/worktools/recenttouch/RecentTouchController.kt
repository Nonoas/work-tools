package indi.nonoas.worktools.recenttouch

import indi.nonoas.worktools.platform.ext.FuncPaneFactory
import javafx.scene.Parent

/**
 * @author Nonoas
 * @datetime 2022/5/12 21:06
 */
class RecentTouchController : FuncPaneFactory {
    override fun getRootView(): Parent {
        return RecentTouchPane.instance
    }

    override fun getCode(): String {
        return "RecentTouch"
    }

    override fun getName(): String {
        return "最近"
    }
}