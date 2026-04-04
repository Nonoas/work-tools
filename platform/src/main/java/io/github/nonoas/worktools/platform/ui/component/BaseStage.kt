package io.github.nonoas.worktools.platform.ui.component

import github.nonoas.jfx.flat.ui.stage.AppStage
import javafx.scene.image.Image
import java.util.Collections

/**
 * 程序通用窗口，设置了一系列通用的样式和参数
 *
 * @author Nonoas
 * @datetime 2022/1/22 22:15
 */
open class BaseStage : github.nonoas.jfx.flat.ui.stage.AppStage() {

    protected val TITLE = "WorkTools"

    init {
        setTitle(TITLE)
        stage.scene.stylesheets.addAll("css/platform.css")
        addIcons(Collections.singleton(Image("image/logo.png")))
    }

}
