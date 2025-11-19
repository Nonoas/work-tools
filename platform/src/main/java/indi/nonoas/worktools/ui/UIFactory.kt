package indi.nonoas.worktools.ui

import github.nonoas.jfx.flat.ui.theme.Styles
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ToggleButton

/**
 * UI 工厂，用于生成常用的 UI 组件
 *
 * @author Nonoas
 * @datetime 2022/1/7 23:42
 */
object UIFactory {
    /**
     * 返回一个指定了 css 样式的 Scene
     *
     * @param parent 根节点
     */
    fun getBaseScene(parent: Parent?): Scene {
        val scene = Scene(parent)
        scene.stylesheets.add("css/style.css")
        return scene
    }

    /**
     * 返回一个按钮，添加 css 样式类 “primary”
     *
     * @param text 按钮文本
     */
    fun getPrimaryButton(text: String?): Button {
        val btn = Button(text)
        btn.styleClass.add(Styles.ACCENT)
        return btn
    }

}
