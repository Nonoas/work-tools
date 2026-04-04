package io.github.nonoas.worktools.platform.ext

import io.github.nonoas.worktools.platform.common.ColorSerials
import io.github.nonoas.worktools.platform.global.ExtensionManager
import io.github.nonoas.worktools.platform.ui.component.FontIconView
import javafx.scene.Node
import javafx.scene.paint.Color
import org.kordamp.ikonli.material2.Material2AL

/**
 * @author Nonoas
 * @datetime 2022/5/12 20:58
 */
interface FuncPaneFactory {
    /**
     * 获取 UI 控制器绑定的根视图
     *
     * @return UI 控制器绑定的根视图
     */
    fun create(): FuncPane

    fun getCode(): String {
        return javaClass.simpleName
    }

    /**
     * 功能面板的名称
     */
    fun getName(): String

    fun getDescription(): String

    fun getGraphic(): Node {
        return FontIconView(
            Material2AL.EXTENSION,
            40,
            Color.web(ColorSerials.GREEN.secureRandomColor)
        )
    }

    companion object {
        fun getAllImpls(): List<FuncPaneFactory> {
            return ExtensionManager.getExtensions(FuncPaneFactory::class.java)
                ?: return listOf()
        }
    }

}