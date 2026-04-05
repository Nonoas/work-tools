package io.github.nonoas.worktools.platform.ui.component

import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.image.Image
import javafx.stage.Stage

open class MyAlert(alertType: AlertType?, contentText: String?, vararg buttons: ButtonType?) :
    javafx.scene.control.Alert(alertType, contentText, *buttons) {

    init {
        initView()
    }

    private fun initView() {
        headerText = null
        val logoUrl = javaClass.getResource("/image/logo.png")?.toExternalForm() ?: ""
        val image = Image(logoUrl)
        val pane = dialogPane
        pane.stylesheets.add("css/platform.css")
        val stage = pane.scene.window as Stage
        stage.isAlwaysOnTop = true
        stage.icons.add(image)
    }
}
