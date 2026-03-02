package indi.nonoas.worktools.platform.ext

import indi.nonoas.worktools.platform.global.Disposable
import javafx.scene.Parent

abstract class FuncPane : Disposable {

    /** 插件 UI */
    abstract fun getRootView(): Parent
}
