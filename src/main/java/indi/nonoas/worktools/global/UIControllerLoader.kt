package indi.nonoas.worktools.global

import indi.nonoas.worktools.common.FuncCode
import indi.nonoas.worktools.ui.UIController
import indi.nonoas.worktools.utils.ClassUtil
import java.lang.reflect.InvocationTargetException

/**
 * @author Nonoas
 * @date 2022/7/16
 */
object UIControllerLoader {
    private const val PKG_DIR = "indi.nonoas.worktools.controller"

    /**
     * 加载所有的 UIController
     *
     * @return K-FuncName，V-对应的 UIController实现类
     */
    @JvmStatic
    @Throws(
        InstantiationException::class,
        IllegalAccessException::class,
        NoSuchMethodException::class,
        InvocationTargetException::class
    )
    fun load(): Map<String, UIController> {
        val map: MutableMap<String, UIController> = HashMap()
        // 查询 PKG_DIR 包下 UIController 的子类
        val classes = ClassUtil.getClassesByPkgName(PKG_DIR).stream()
            .filter { cls -> UIController::class.java.isAssignableFrom(cls) }
            .toList()
        for (aClass in classes) {
            val funcCode = aClass.getAnnotation(FuncCode::class.java)
            if (null != funcCode) {
                map[funcCode.value] = ClassUtil.newInstance(aClass) as UIController
            }
        }
        return map
    }
}
