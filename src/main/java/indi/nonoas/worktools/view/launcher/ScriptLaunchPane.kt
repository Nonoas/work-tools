package indi.nonoas.worktools.view.launcher

import com.kodedu.terminalfx.TerminalTab
import javafx.scene.control.TabPane

import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

/**
 * .bat的快速执行程序
 *
 * @author Nonoas
 * @date 2021/9/4
 */
class ScriptLaunchPane(spacing: Double) : VBox(spacing) {

    private constructor() : this(16.0) {
        initView()
    }

    private fun initView() {
        val terminalBuilder = com.kodedu.terminalfx.TerminalBuilder()
        val terminal: TerminalTab = terminalBuilder.newTerminal()

        val tabPane = TabPane()
        tabPane.tabs.add(terminal)

        setVgrow(tabPane, Priority.ALWAYS)
        children.add(tabPane)
    }

    companion object {
        //同步代码块
        //对外提供获取实例对象的方法
        @Volatile
        var instance: ScriptLaunchPane? = null
            get() {
                if (field != null) return field
                //同步代码块
                synchronized(ScriptLaunchPane::class.java) {
                    if (field == null) {
                        field = ScriptLaunchPane()
                    }
                }
                return field
            }
            private set
    }
}