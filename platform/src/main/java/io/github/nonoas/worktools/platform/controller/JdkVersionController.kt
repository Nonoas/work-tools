package io.github.nonoas.worktools.platform.controller

import github.nonoas.jfx.flat.ui.pane.SVGImage
import io.github.nonoas.worktools.platform.common.ColorSerials
import io.github.nonoas.worktools.platform.ext.FuncPane
import io.github.nonoas.worktools.platform.view.env.JdkVersionPane
import javafx.scene.Node
import javafx.scene.paint.Color

/**
 * TODO 类描述
 * @author huangshengsheng
 * @date 2024/5/24 10:45
 */
class JdkVersionController: BaseParentController() {

    override fun getCode(): String {
        return "JdkVersion"
    }

    override fun getName(): String {
        return "JDK版本管理"
    }

    override fun getGraphic(): Node {
        return SVGImage("""
            M896 0H128C57.6 0 0 57.6 0 128v768c0 70.4 57.6 128 128 128h768c70.4 0 128-57.6 128-128V128c0-70.4-57.6-128-128-128z 
            m-121.6 537.6c12.8 32 0 70.4-51.2 96 44.8-57.6 32-108.8-25.6-115.2 32-19.2 64-12.8 76.8 19.2zM403.2 358.4c6.4-32 
            25.6-57.6 83.2-96l6.4-6.4C563.2 204.8 582.4 172.8 576 128h12.8c25.6 96 6.4 134.4-51.2 185.6l-12.8 6.4c-57.6 
            51.2-70.4 83.2-19.2 179.2l-12.8 6.4C422.4 448 390.4 403.2 403.2 358.4z m153.6 25.6c0 12.8 6.4 25.6 12.8 44.8 
            12.8 44.8 19.2 64-12.8 83.2 0-6.4-6.4-19.2-25.6-51.2C518.4 428.8 512 403.2 512 384c0-25.6 19.2-51.2 64-70.4-19.2 
            19.2-25.6 44.8-19.2 70.4zM396.8 512l6.4 12.8c-38.4 12.8-44.8 25.6-32 32 6.4 12.8 38.4 19.2 83.2 19.2 89.6 0 
            192-12.8 224-38.4l12.8 6.4c-25.6 76.8-160 102.4-288 83.2-57.6-12.8-102.4-25.6-108.8-44.8-6.4-32 25.6-51.2 102.4-70.4z 
            m224 160c-25.6 38.4-83.2 57.6-140.8 51.2-32 0-57.6-12.8-64-19.2-12.8-6.4-12.8-19.2 0-32 25.6 38.4 121.6 32 204.8 
            0zM384 844.8c-89.6-6.4-153.6-19.2-179.2-44.8-32-25.6 0-57.6 96-89.6l6.4 12.8c-32 25.6-25.6 38.4 6.4 51.2 32 
            12.8 89.6 12.8 160 12.8 140.8-6.4 300.8-32 352-57.6l6.4 6.4c-44.8 83.2-243.2 121.6-448 108.8z
        """.trimIndent(), Color.web(ColorSerials.GREEN.secureRandomColor)).apply {
            prefWidth = 32.0
            prefHeight = 32.0
        }
    }

    override fun create(): FuncPane {
        return JdkVersionPane()
    }
}