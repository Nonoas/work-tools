package indi.nonoas.worktools.website

import indi.nonoas.worktools.platform.ext.FuncPaneFactory
import javafx.scene.Parent

/**
 * @author Nonoas
 * @datetime 2022/5/12 20:48
 */
class BootWebsiteController : FuncPaneFactory {
    override fun getRootView(): Parent {
        return BootWebsitePane()
    }

    override fun getCode(): String {
        return "indi.nonoas.worktools.website.BootWebsiteController"
    }

    override fun getName(): String {
        return "网站书签"
    }
}