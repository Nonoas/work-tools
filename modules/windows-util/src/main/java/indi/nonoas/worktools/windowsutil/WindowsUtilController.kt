package indi.nonoas.worktools.launcher

import indi.nonoas.worktools.platform.ext.FuncPaneFactory
import indi.nonoas.worktools.windowsutil.WindowTablePane
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.image.ImageView


/**
 * @author Nonoas
 * @datetime 2022/5/12 20:48
 */
class WindowsUtilController : FuncPaneFactory {
    override fun getRootView(): Parent {
        return WindowTablePane.getInstance()
    }

    override fun getName(): String {
        return "Windows工具"
    }

    override fun getDescription(): String {
        return "窗口置顶等工具类"
    }

    override fun getGraphic(): Node {
        return ImageView("images/windows64x64.png")
            .apply {
                isPreserveRatio = true;
                isSmooth = true
                fitWidth = 32.0
                fitHeight = 32.0
            }
    }
}