package indi.nonoas.worktools.launcher

import indi.nonoas.worktools.platform.ext.FuncPaneFactory
import indi.nonoas.worktools.platform.ui.component.FontIconView
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.paint.Color
import org.kordamp.ikonli.material2.Material2MZ

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

    override fun getGraphic(): Node {
        return FontIconView(Material2MZ.ROCKET, 40, Color.BLACK)
    }
}