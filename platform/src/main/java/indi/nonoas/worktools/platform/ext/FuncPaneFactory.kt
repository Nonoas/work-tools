package indi.nonoas.worktools.platform.ext

import indi.nonoas.worktools.platform.global.ExtensionManager
import indi.nonoas.worktools.platform.ui.component.FontIconView
import javafx.scene.Node
import javafx.scene.Parent
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
    fun getRootView(): Parent

    fun getCode(): String {
        return javaClass.simpleName
    }

    fun getName(): String {
        return getCode()
    }

    fun getGraphic(): Node {
        return FontIconView(Material2AL.EXTENSION, 40, Color.BLACK)
    }

    companion object {
        fun getAllImpls(): List<FuncPaneFactory> {
            return ExtensionManager.getExtensions(FuncPaneFactory::class.java)
                ?: return listOf()
        }
    }

}