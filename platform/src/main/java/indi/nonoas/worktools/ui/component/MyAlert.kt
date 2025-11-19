package indi.nonoas.worktools.ui.component

import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.image.Image
import javafx.stage.Stage

open class MyAlert(alertType: AlertType?, contentText: String?, vararg buttons: ButtonType?) :
    Alert(alertType, contentText, *buttons) {

    init {
        initView()
    }

    private fun initView() {
        headerText = null
        val image = Image("image/logo.png")
        val pane = dialogPane
        pane.stylesheets.add("css/style.css")
        val stage = pane.scene.window as Stage
        stage.isAlwaysOnTop = true
        stage.icons.add(image)
    }
}