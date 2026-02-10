package indi.nonoas.worktools.platform.ext

import indi.nonoas.worktools.platform.global.Disposable
import javafx.scene.Parent

interface FuncPane : Disposable {

    /** 插件 UI */
    fun getRootView(): Parent

    /** 释放资源（消息订阅、监听器、线程等） */
    override fun dispose()
}
