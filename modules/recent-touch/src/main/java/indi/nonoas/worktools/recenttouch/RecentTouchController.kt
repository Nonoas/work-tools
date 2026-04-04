package indi.nonoas.worktools.recenttouch

import io.github.nonoas.worktools.platform.common.ColorSerials
import io.github.nonoas.worktools.platform.common.Colors
import io.github.nonoas.worktools.platform.ext.FuncPane
import io.github.nonoas.worktools.platform.ext.FuncPaneFactory
import io.github.nonoas.worktools.platform.ui.component.FontIconView
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.paint.Color
import org.kordamp.ikonli.material2.Material2MZ

/**
 * @author Nonoas
 * @datetime 2022/5/12 21:06
 */
class RecentTouchController : FuncPaneFactory {
    override fun create(): FuncPane {
        return RecentTouchPane.instance
    }

    override fun getCode(): String {
        return "RecentTouch"
    }

    override fun getName(): String {
        return "最近"
    }

    override fun getDescription(): String {
        return "最近打开的文件"
    }

    override fun getGraphic(): Node {
        return FontIconView(Material2MZ.TIMER, 40, Color.web(ColorSerials.GREEN.secureRandomColor))
    }
}