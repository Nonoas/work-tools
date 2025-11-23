package indi.nonoas.worktools.platform.ui

import indi.nonoas.worktools.platform.global.ExtensionManager
import javafx.scene.Parent

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

    companion object {
        fun getAllImpls(): List<FuncPaneFactory> {
            return ExtensionManager.getExtensions(FuncPaneFactory::class.java)
                ?: return listOf()
        }
    }
}
