package indi.nonoas.worktools.website

import io.github.nonoas.worktools.platform.ext.FuncPane
import io.github.nonoas.worktools.platform.ext.FuncPaneFactory
import javafx.scene.Node
import javafx.scene.image.ImageView

/**
 * @author Nonoas
 * @datetime 2022/5/12 20:48
 */
class BootWebsiteController : FuncPaneFactory {

    override fun create(): FuncPane {
        val bootWebsitePane = BootWebsitePane()
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
        val imageUrl = javaClass.getResource("/images/website.png")?.toExternalForm() ?: ""
        return ImageView(imageUrl)
            .apply {
                isPreserveRatio = true;
                isSmooth = true
                fitWidth = 32.0
                fitHeight = 32.0
            }
    }

}