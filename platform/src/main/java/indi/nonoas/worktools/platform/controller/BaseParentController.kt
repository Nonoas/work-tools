package indi.nonoas.worktools.platform.controller

import indi.nonoas.worktools.platform.common.DataBinder.getBindModel
import indi.nonoas.worktools.platform.ext.FuncPane
import indi.nonoas.worktools.platform.ext.FuncPaneFactory
import javafx.scene.Parent

/**
 * @author Nonoas
 * @datetime 2022/5/12 20:49
 */
open class BaseParentController : FuncPaneFactory {

    /**
     * 将 tClass 的数据类与 rootView 的组件属性绑定
     *
     * @param tClass 数据类 class
     * @param <T>    数据类类型
     * @return 数据类实例
    </T> */
    protected fun <T> getBindModel(tClass: Class<T>): T? {
        return null
    }

    override fun create(): FuncPane {
        TODO("Not yet implemented")
    }

    override fun getName(): String {
        return getCode()
    }

    override fun getDescription(): String {
        return getCode()
    }
}
