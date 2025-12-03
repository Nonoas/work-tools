package indi.nonoas.worktools.launcher

import indi.nonoas.worktools.platform.common.ColorSerials
import indi.nonoas.worktools.platform.ext.FuncPaneFactory
import indi.nonoas.worktools.platform.ui.component.FontIconView
import indi.nonoas.worktools.windowsutil.WindowTablePane
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.paint.Color
import org.kordamp.ikonli.material2.Material2MZ

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
        return FontIconView(Material2MZ.ROCKET, 40, Color.web(ColorSerials.GREEN.secureRandomColor))
    }
}