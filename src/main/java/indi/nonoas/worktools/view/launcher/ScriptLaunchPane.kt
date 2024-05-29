package indi.nonoas.worktools.view.launcher

import indi.nonoas.worktools.common.CommonInsets
import indi.nonoas.worktools.pojo.vo.ExecFilePo
import indi.nonoas.worktools.ui.component.ExceptionAlter
import javafx.event.EventHandler
import javafx.scene.control.Button
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
class ScriptLaunchPane(spacing: Double) : VBox(spacing) {

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
    private val dragDropHandler = EventHandler { event: DragEvent ->
        val dragboard = event.dragboard
        if (!dragboard.hasFiles()) return@EventHandler
        try {
            for (f in dragboard.files) {
                val po = ExecFilePo().apply {
                    name = f.name
                    link = f.absolutePath
                    lastUseTimestamp = System.currentTimeMillis()
                    createTimestamp = lastUseTimestamp
                }
                addExecFile(po)
            }
        } catch (e: Exception) {
            ExceptionAlter(e).showAndWait()
            logger.error("添加执行文件失败", e)
        }
    }

    private constructor() : this(16.0) {
        initView()
    }

    private fun initView() {
        flowPane.apply {
            onDragOver = dragOverHandler
            onDragDropped = dragDropHandler
        }
        setVgrow(flowPane, Priority.ALWAYS)
        children.setAll(flowPane)
    }

    private fun addExecFile(po: ExecFilePo) {
        flowPane.children.add(0, Button("a$po"))
    }


    companion object {
        private val logger = LogManager.getLogger(ScriptLaunchPane::class.java)

        //同步代码块
        //对外提供获取实例对象的方法
        @Volatile
        var instance: ScriptLaunchPane? = null
            get() {
                if (field != null) return field
                //同步代码块
                synchronized(ScriptLaunchPane::class.java) {
                    if (field == null) {
                        field = ScriptLaunchPane()
                    }
                }
                return field
            }
            private set
    }
}