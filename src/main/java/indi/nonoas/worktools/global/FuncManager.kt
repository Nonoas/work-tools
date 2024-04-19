package indi.nonoas.worktools.global

import indi.nonoas.worktools.global.UIControllerLoader.load
import indi.nonoas.worktools.ui.UIController
import indi.nonoas.worktools.ui.component.ExceptionAlter
import javafx.scene.Parent
import java.lang.reflect.InvocationTargetException

/**
 * 功能面板枚举，将工具栏中使用按钮切换的面板注册在此
 *
 * @author Nonoas
 * @datetime 2022/5/12 20:03
 */
object FuncManager {
    private lateinit var map: Map<String, UIController>

    init {
        try {
            map = load()
        } catch (e: InstantiationException) {
            ExceptionAlter.error(e)
        } catch (e: IllegalAccessException) {
            ExceptionAlter.error(e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e)
        } catch (e: NoSuchMethodException) {
            throw RuntimeException(e)
        }
    }

    fun getRootView(funcCode: String): Parent? {
        val uiController = map[funcCode]
        return uiController?.getRootView()
    }
}
