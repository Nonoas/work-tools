package indi.nonoas.worktools.view.launcher

import indi.nonoas.worktools.common.CommonInsets
import indi.nonoas.worktools.dao.ExecFileDao
import indi.nonoas.worktools.pojo.vo.ExecFileVo
import indi.nonoas.worktools.ui.TaskHandler
import indi.nonoas.worktools.utils.UIUtil
import javafx.event.EventHandler
import javafx.scene.image.ImageView
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.FlowPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.apache.logging.log4j.LogManager

/**
 * .bat的快速执行程序
 *
 * @author Nonoas
 * @date 2021/9/4
 */
class ExecFilePane(spacing: Double) : VBox(spacing) {

    private val flowPane = FlowPane(CommonInsets.SPACING_1, CommonInsets.SPACING_1)

    /**
     * 文件拖入模式设置
     */
    private val dragOverHandler = EventHandler { event: DragEvent ->
        if (event.gestureSource !== this) {
            event.acceptTransferModes(TransferMode.MOVE)
        }
    }

    /**
     * 拖动文件松开后的操作
     */
    @Suppress("UNCHECKED_CAST")
    private val dragDropHandler = EventHandler { event: DragEvent ->
        val files = event.dragboard.files ?: return@EventHandler

        // 添加到面板
        val buttons = files.map {
            ExecFileButton().apply {
                text = it.name
                graphic = ImageView(UIUtil.getFileIcon(it))
            }
        }
        flowPane.children.addAll(0, buttons)
        // 添加到数据库
        TaskHandler.backRun {
            val params = files.map {
                val time = System.currentTimeMillis()
                arrayOf(it.name, it.absolutePath, time, time)
            } as MutableList<Array<Any>>
            ExecFileDao.insertBatch(params)
        }

    }

    private constructor() : this(16.0) {
        initView()
    }

    private fun initView() {
        padding = CommonInsets.PADDING_20

        flowPane.apply {
            onDragOver = dragOverHandler
            onDragDropped = dragDropHandler
        }
        setVgrow(flowPane, Priority.ALWAYS)
        children.setAll(flowPane)

        TaskHandler<List<ExecFileVo>>().whenCall {
            ExecFileDao.findAll()
        }.andThen { vos ->
            val buttons = vos.map { ExecFileButton(it) }
            flowPane.children.addAll(buttons)
        }.handle()

    }

    companion object {
        private val logger = LogManager.getLogger(ExecFilePane::class.java)

        // 同步代码块
        // 对外提供获取实例对象的方法
        @Volatile
        var instance: ExecFilePane? = null
            get() {
                if (field != null) return field
                // 同步代码块
                synchronized(ExecFilePane::class.java) {
                    if (field == null) {
                        field = ExecFilePane()
                    }
                }
                return field
            }
            private set
    }
}