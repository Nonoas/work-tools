package indi.nonoas.worktools.website

import indi.nonoas.worktools.platform.ext.FuncPane
import indi.nonoas.worktools.platform.ext.FuncPaneFactory
import javafx.scene.Node
import javafx.scene.image.ImageView

/**
 * @author Nonoas
 * @datetime 2022/5/12 20:48
 */
class BootWebsiteController : FuncPaneFactory {
    override fun create(): FuncPane {
        val bootWebsitePane = BootWebsitePane()
        bootWebsitePane.initSearchEvent()
        return bootWebsitePane
    }

    override fun getCode(): String {
        return "indi.nonoas.worktools.website.BootWebsiteController"
    }

    override fun getName(): String {
        return "网站书签"
    }

    override fun getDescription(): String {
        return "通过命令快速打开网页"
    }

    override fun getGraphic(): Node {
        return ImageView("images/website.png")
            .apply {
                isPreserveRatio = true;
                isSmooth = true
                fitWidth = 32.0
                fitHeight = 32.0
            }
    }

}