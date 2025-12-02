package indi.nonoas.worktools.website

import indi.nonoas.worktools.platform.common.ColorSerials
import indi.nonoas.worktools.platform.common.Colors
import indi.nonoas.worktools.platform.ext.FuncPaneFactory
import indi.nonoas.worktools.platform.ui.component.FontIconView
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.paint.Color
import org.kordamp.ikonli.material2.Material2AL
import org.kordamp.ikonli.material2.Material2MZ

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

    override fun getGraphic(): Node {
        return FontIconView(Material2AL.BOOK, 40, Color.web(ColorSerials.GREEN.secureRandomColor))
    }
}