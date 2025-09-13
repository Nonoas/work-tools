package indi.nonoas.worktools.view

import indi.nonoas.worktools.global.Manifest
import indi.nonoas.worktools.ui.component.MyAlert

/**
 * @author Nonoas
 * @date 2021/9/4
 */
class AboutAlerts private constructor(contentText: String?) : MyAlert(AlertType.INFORMATION, contentText) {

    init {
        title = "关于"
        graphic = null
        dialogPane.style = "-fx-font-family: \"Source Code Pro\""
    }

    companion object {
        //同步代码块
        //对外提供获取实例对象的方法
        //声明私有静态对象，用volatile修饰
        @Volatile
        var instance: AboutAlerts? = null
            get() {
                if (field != null) return field
                //同步代码块
                synchronized(AboutAlerts::class.java) {
                    if (field == null) {
                        field = AboutAlerts(getDesc())
                    }
                }
                return field
            }
            private set

        private fun getDesc(): String {
            val version = Manifest.get("app.version")
            return """
                
                WorkTools
                
                Created     By Nonoas
                Contributed By WorkTool项目小组
                version:$version
            """.trimIndent()
        }
    }


}