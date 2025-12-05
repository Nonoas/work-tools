package indi.nonoas.worktools.launcher

import indi.nonoas.worktools.platform.common.ColorSerials
import indi.nonoas.worktools.platform.common.Colors
import indi.nonoas.worktools.platform.ext.FuncPaneFactory
import indi.nonoas.worktools.platform.ui.component.FontIconView
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.image.ImageView
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

    override fun getName(): String {
        return "快速启动"
    }

    override fun getDescription(): String {
        return "快速打开文件或执行程序"
    }

    override fun getGraphic(): Node {
        return ImageView("images/rocket.png")
            .apply {
                isPreserveRatio = true;
                isSmooth = true
                fitWidth = 32.0
                fitHeight = 32.0
            }
    }
}