package io.github.nonoas.worktools.platform.ext

import io.github.nonoas.worktools.platform.global.Disposable
import javafx.scene.Parent

abstract class FuncPane : Disposable {

    /** 插件 UI */
    abstract fun getRootView(): Parent
}
