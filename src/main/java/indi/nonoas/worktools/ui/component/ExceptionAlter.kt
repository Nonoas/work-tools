package indi.nonoas.worktools.ui.component

import javafx.scene.control.Alert
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.image.Image
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.io.PrintWriter
import java.io.StringWriter

/**
 * 程序异常弹窗提醒
 *
 * @author Nonoas
 * @datetime 2021/12/22 21:44
 */
class ExceptionAlter(e: Throwable) : Alert(AlertType.ERROR, e.message) {
    init {
        initView(e)
    }

    private fun initView(e: Throwable) {
        initAlterStage(e)
        val sw = StringWriter()
        e.printStackTrace(PrintWriter(sw))
        val exceptionText = sw.toString()
        val label = Label("异常堆栈跟踪如下:")
        val textArea = TextArea(exceptionText)
        textArea.isEditable = false
        textArea.isWrapText = true
        VBox.setVgrow(textArea, Priority.ALWAYS)
        val expContent = VBox()
        expContent.maxWidth = Double.MAX_VALUE
        expContent.children.addAll(label, textArea)
        dialogPane.expandableContent = expContent
    }

    /**
     * 初始化窗口相关
     *
     * @param e 异常
     */
    private fun initAlterStage(e: Throwable) {
        title = "程序异常"
        headerText = "ლ(ٱ٥ٱლ)，程序出现了一些问题"
        contentText = e.message
        val image = Image("image/logo.png")
        val pane = dialogPane
        pane.stylesheets.add("css/bootstrap3.css")
        val stage = pane.scene.window as Stage
        stage.setAlwaysOnTop(true)
        stage.minWidth = 300.0
        stage.icons.add(image)
    }

    companion object {
        @JvmStatic
        fun error(e: Throwable) {
            ExceptionAlter(e).showAndWait()
        }
    }
}
