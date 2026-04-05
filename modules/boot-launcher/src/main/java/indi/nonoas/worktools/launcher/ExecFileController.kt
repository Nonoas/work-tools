package indi.nonoas.worktools.launcher

import io.github.nonoas.worktools.platform.ext.FuncPane
import io.github.nonoas.worktools.platform.ext.FuncPaneFactory
import javafx.scene.Node
import javafx.scene.image.ImageView

/**
 * @author Nonoas
 * @datetime 2022/5/12 20:48
 */
class ExecFileController : FuncPaneFactory {
    override fun create(): FuncPane {
        return ExecFilePane()
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
        val imageUrl = javaClass.getResource("/images/rocket.png")?.toExternalForm() ?: ""
        return ImageView(imageUrl)
            .apply {
                isPreserveRatio = true;
                isSmooth = true
                fitWidth = 32.0
                fitHeight = 32.0
            }
    }
}