package indi.nonoas.worktools.ui.component

import github.nonoas.jfx.flat.ui.stage.AppStage
import javafx.scene.image.Image
import javafx.stage.Stage
import java.util.*

/**
 * 程序通用窗口，设置了一系列通用的样式和参数
 *
 * @author Nonoas
 * @datetime 2022/1/22 22:15
 */
open class BaseStage : AppStage() {

    protected val TITLE = "WorkTools"

    init {
        setTitle(TITLE)
//        stage.scene.stylesheets.addAll("css/bootstrap3.css")
        addIcons(Collections.singleton(Image("image/logo.png")))
    }

}
