package indi.nonoas.worktools.view.fileencode

import cn.hutool.core.io.FileUtil
import indi.nonoas.worktools.utils.FileUtil as FU
import cn.hutool.core.util.CharsetUtil
import indi.nonoas.worktools.common.CommonInsets
import indi.nonoas.worktools.ui.TaskHandler
import indi.nonoas.worktools.ui.UIFactory
import indi.nonoas.worktools.ui.component.MyAlert
import javafx.event.EventHandler
import javafx.scene.control.Alert
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import java.io.File
import java.nio.charset.Charset

/**
 * 文件编码功能主面板
 *
 * @author Nonoas
 * @date 2024/3/30 22:46
 */
class FileEncodePane : VBox(10.0) {

    private val tf_dirPath = TextField().apply {
        promptText = "源文件/文件夹"
        HBox.setHgrow(this, Priority.ALWAYS)
    }
    private val btn_brow = UIFactory.getPrimaryButton("目录")
    private val ccb_tarCharset = CharsetComboBox().apply { promptText = "目标编码" }
    private val btn_Run = UIFactory.getPrimaryButton("转换")

    init {
        padding = CommonInsets.PADDING_20

        btn_brow.onAction = EventHandler { onDirPathBrow() }
        btn_Run.onAction = EventHandler { onRun() }

        val hBox = HBox(10.0, tf_dirPath, btn_brow)
        val hb_charset = HBox(10.0, ccb_tarCharset)

        children.addAll(hBox, hb_charset, btn_Run)
    }

    private fun onRun() {
        val file = File(tf_dirPath.text)
        if (!file.exists()) {
            return
        }
        TaskHandler<Unit>().whenCall { convertFile(file) }
            .andThen{ MyAlert(Alert.AlertType.INFORMATION,"转换成功").show()}
            .handle()
    }

    private fun convertFile(file: File) {
        if (file.isFile) {
            val charsetName = FU.detectCharset(file)
            FileUtil.convertCharset(file, CharsetUtil.charset(charsetName), ccb_tarCharset.value)
            return
        }
        // 处理文件夹
        for (f in file.listFiles()) {
            convertFile(f)
        }
    }

    private fun onDirPathBrow() {
        val fileChooser = DirectoryChooser()
        val file = fileChooser.showDialog(scene.window) ?: return
        tf_dirPath.text = file.absolutePath
    }


    companion object {
        //同步代码块
        //对外提供获取实例对象的方法
        //声明私有静态对象，用volatile修饰
        @Volatile
        var instance: FileEncodePane? = null
            get() {
                if (field != null) return field
                //同步代码块
                synchronized(FileEncodePane::class.java) {
                    if (field == null) {
                        field = FileEncodePane()
                    }
                }
                return field
            }
            private set
    }

    class CharsetComboBox : ComboBox<Charset>() {
        init {
            items.addAll(
                CharsetUtil.CHARSET_UTF_8,
                CharsetUtil.CHARSET_GBK,
                CharsetUtil.CHARSET_ISO_8859_1
            )
        }
    }
}
