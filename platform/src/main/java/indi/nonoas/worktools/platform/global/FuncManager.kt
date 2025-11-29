package indi.nonoas.worktools.platform.global

import indi.nonoas.worktools.platform.ext.FuncPaneFactory
import javafx.scene.Parent
import org.apache.logging.log4j.LogManager

/**
 * 功能面板枚举，将工具栏中使用按钮切换的面板注册在此
 *
 * @author Nonoas
 * @datetime 2022/5/12 20:03
 */
object FuncManager {

    private val LOG = LogManager.getLogger(FuncManager.javaClass)

    private lateinit var map: Map<String, FuncPaneFactory>

    init {
        try {
            map = load()
        } catch (e: Exception) {
            LOG.error("加载功能面板失败", e)
        }
    }

    private fun load(): Map<String, FuncPaneFactory> {

        val map = LinkedHashMap<String, FuncPaneFactory>()
        for (factory in FuncPaneFactory.getAllImpls()) {
            map[factory.getCode()] = factory
        }
        return map
    }

    fun getRootView(funcCode: String): Parent? {
        val uiController = map[funcCode]
        return uiController?.getRootView()
    }
}
