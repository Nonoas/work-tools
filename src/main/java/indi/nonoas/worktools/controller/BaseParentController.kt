package indi.nonoas.worktools.controller

import indi.nonoas.worktools.common.DataBinder.getBindModel
import indi.nonoas.worktools.ui.UIController
import javafx.scene.Parent

/**
 * @author Nonoas
 * @datetime 2022/5/12 20:49
 */
open class BaseParentController : UIController {

    /**
     * 将 tClass 的数据类与 rootView 的组件属性绑定
     *
     * @param tClass 数据类 class
     * @param <T>    数据类类型
     * @return 数据类实例
    </T> */
    protected fun <T> getBindModel(tClass: Class<T>): T? {
        return getBindModel(getRootView(), tClass)
    }

    override fun getRootView(): Parent {
        TODO("Not yet implemented")
    }
}
