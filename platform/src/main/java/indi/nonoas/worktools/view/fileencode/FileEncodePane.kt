import cn.hutool.core.io.FileUtil
import cn.hutool.core.util.CharsetUtil
import indi.nonoas.worktools.common.CommonInsets
import indi.nonoas.worktools.ui.TaskHandler
import indi.nonoas.worktools.ui.UIFactory
import indi.nonoas.worktools.ui.component.MyAlert
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.control.Alert
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import java.io.File
import java.nio.charset.Charset
import java.util.concurrent.atomic.AtomicBoolean
import indi.nonoas.worktools.utils.FileUtil as FU

class FileEncodePane : VBox(10.0) {

    private val tf_dirPath = TextField().apply {
        promptText = "源文件/文件夹"
        HBox.setHgrow(this, Priority.ALWAYS)
    }
    private val btn_brow = UIFactory.getPrimaryButton("目录")
    private val ccb_tarCharset = CharsetComboBox().apply { promptText = "目标编码" }
    private val btn_Run = UIFactory.getPrimaryButton("转换")

    // ====== 日志区域（优化版）======
    private val logArea = TextArea().apply {
        isEditable = false
        prefRowCount = 12
        isWrapText = true // ✅ 正确写法
        setVgrow(this, Priority.ALWAYS)
    }

    // 最大保留行数（可按需调整）
    private val maxLogLines = 1000

    // 可选：最大字符数上限（0 表示不启用字符上限）
    private val maxTotalChars = 0

    // 行环形缓冲与刷新合并标志
    private val logLines: ArrayDeque<String> = ArrayDeque(maxLogLines + 64)
    private val flushScheduled = AtomicBoolean(false)

    init {
        padding = CommonInsets.PADDING_20

        btn_brow.onAction = EventHandler { onDirPathBrow() }
        btn_Run.onAction = EventHandler { onRun() }

        val hBox = HBox(10.0, tf_dirPath, btn_brow)
        val hb_charset = HBox(10.0, ccb_tarCharset)

        children.addAll(hBox, hb_charset, btn_Run, logArea)
    }

    private fun onRun() {
        val file = File(tf_dirPath.text)
        if (!file.exists()) {
            appendLog("路径不存在: ${file.absolutePath}")
            return
        }
        appendLog("开始转换: ${file.absolutePath} -> 目标编码 ${ccb_tarCharset.value?.displayName() ?: "未选择"}")
        TaskHandler<Unit>()
            .whenCall { convertFile(file) }
            .andThen {
                appendLog("转换完成")
                MyAlert(Alert.AlertType.INFORMATION, "转换成功").show()
            }
            .handle()
    }

    private fun convertFile(file: File) {
        if (file.isFile) {
            val charsetName = FU.detectCharset(file)
            FileUtil.convertCharset(file, CharsetUtil.charset(charsetName), ccb_tarCharset.value)
            appendLog("已转换: ${file.absolutePath} (${charsetName} -> ${ccb_tarCharset.value})")
            return
        }
        // 处理文件夹（为空判空）
        file.listFiles()?.forEach { f -> convertFile(f) }
    }

    // ====== 优化后的日志追加与批量刷新 ======
    private fun appendLog(msg: String) {
        val ts = java.time.LocalTime.now().withNano(0)
        val line = "[$ts] $msg"
        synchronized(logLines) {
            logLines.addLast(line)
            // 行数上限控制
            while (logLines.size > maxLogLines) {
                logLines.removeFirst()
            }
            // 可选：字符数上限控制（在需要时启用）
            if (maxTotalChars > 0) {
                var total = 0
                // 正向累计全部字符数（含换行）
                val it = logLines.iterator()
                while (it.hasNext()) {
                    total += it.next().length + 1 // +1 for '\n'
                }
                // 超出上限则从头部移除旧日志
                while (total > maxTotalChars && logLines.isNotEmpty()) {
                    val removed = logLines.removeFirst()
                    total -= removed.length + 1
                }
            }
        }
        // 合并刷新——只安排一次 runLater，避免频繁 UI 更新
        scheduleFlush()
    }


    private fun scheduleFlush() {
        if (flushScheduled.compareAndSet(false, true)) {
            Platform.runLater {
                val snapshot: List<String> = synchronized(logLines) { logLines.toList() }
                // 一次性重建文本，保证裁剪与显示一致
                val text = buildString(snapshot.sumOf { it.length + 1 }) {
                    snapshot.forEach {
                        append(it).append('\n')
                    }
                }
                logArea.text = text
                logArea.positionCaret(logArea.length) // 滚动到底部
                flushScheduled.set(false)
            }
        }
    }
    // ===================================

    private fun onDirPathBrow() {
        val fileChooser = DirectoryChooser()
        val file = fileChooser.showDialog(scene.window) ?: return
        tf_dirPath.text = file.absolutePath
        appendLog("选择目录: ${file.absolutePath}")
    }

    companion object {
        @Volatile
        var instance: FileEncodePane? = null
            get() {
                if (field != null) return field
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
