package indi.nonoas.worktools.ui.component

import javafx.scene.image.Image
import javafx.stage.Stage

/**
 * 程序通用窗口，设置了一系列通用的样式和参数
 *
 * @author Nonoas
 * @datetime 2022/1/22 22:15
 */
open class BaseStage : Stage() {

    protected val TITLE = "WorkTools"

    init {
        title = TITLE
        icons.add(Image("image/logo.png"))
    }

    val isInsight: Boolean
        /**
         * 判断窗口是否在显示在屏幕上，即没有最小化且没有隐藏
         */
        get() = isShowing && !isIconified

    /**
     * 由于 [Stage.show] 方法不能重写，显示窗口时可能会做一些其他的操作，所以提供此方法。
     * 调用时，如果窗口处于最小化状态，也会显示出来
     */
    open fun display() {
        if (isIconified) setIconified(false)
        super.show()
    }
}
