package indi.nonoas.worktools.view

import indi.nonoas.worktools.common.CommonInsets
import indi.nonoas.worktools.dao.RtpLinkListDao
import indi.nonoas.worktools.pojo.po.RtpLinkListPo
import indi.nonoas.worktools.pojo.vo.RtpLinkListVo
import indi.nonoas.worktools.ui.TaskHandler
import indi.nonoas.worktools.ui.component.ExceptionAlter
import indi.nonoas.worktools.ui.component.MyAlert
import indi.nonoas.worktools.utils.UIUtil
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.Button
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.image.ImageView
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.FlowPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.apache.log4j.Logger
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import javax.swing.ImageIcon
import javax.swing.filechooser.FileSystemView


/**
 * 临时存放最近使用的文件，方便快捷打开
 *
 * @author Nonoas
 * @date 2021/9/5
 */
class RecentTouchPane private constructor() : VBox(10.0) {

    private val flowPane: FlowPane = FlowPane(10.0, 10.0)
    private val openerBtnList: ObservableList<Node> = flowPane.children

    private val rtpLinkListDao = RtpLinkListDao()

    private val logger = Logger.getLogger(RecentTouchPane::class.java)

    private fun initView() {
        alignment = Pos.BOTTOM_RIGHT
        padding = CommonInsets.PADDING_20
        flowPane.apply {
            onDragOver = dragOverHandler
            onDragDropped = dragDropHandler
        }
        children.addAll(flowPane)
        setVgrow(flowPane, Priority.ALWAYS)
        initFromDB()
    }

    private fun initFromDB() {
        TaskHandler<MutableList<RtpLinkListPo>>()
                .whenCall { rtpLinkListDao.getAll() }
                .andThen { pos ->
                    for (po in pos) {
                        openerBtnList.add(OpenerBtn(po.covertVo()))
                    }
                }.handle()
    }


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
                val po = RtpLinkListVo().apply {
                    name = f.name
                    link = f.absolutePath
                    lastUseTimestamp = System.currentTimeMillis()
                }
                addOpenerBtn(po)
            }
        } catch (e: Exception) {
            ExceptionAlter(e).showAndWait()
            logger.error("添加按钮出错", e)
        }
    }

    private fun addOpenerBtn(vo: RtpLinkListVo): Boolean {
        val btn = OpenerBtn(vo)
        for (node in openerBtnList) {
            if (node == btn) {
                return false
            }
        }
        openerBtnList.add(btn)
        TaskHandler<Int>()
                .whenCall { RtpLinkListDao().add(vo.covertPo()) }
                .andThen {}
                .handle()

        return true
    }

    private class OpenerBtn(private val vo: RtpLinkListVo) : Button(vo.name) {
        init {
            val file = vo.link?.let { File(it) }
            val fsv = FileSystemView.getFileSystemView()
            val icon = fsv.getSystemIcon(file, 32, 32) as ImageIcon
            val fxImage = UIUtil.convertImageIconToFXImage(icon)
            graphic = ImageView(fxImage).apply {
                fitWidth = 24.0
                fitHeight = 24.0
                isPreserveRatio = true
                isSmooth = true
            }

            onAction = EventHandler {
                try {
                    Desktop.getDesktop().open(file)

                    val flowPane = parent as FlowPane
                    flowPane.children.remove(this@OpenerBtn)
                    flowPane.children.add(0, this@OpenerBtn)
                } catch (e: IOException) {
                    MyAlert(AlertType.ERROR, "文件打开失败！").show()
                }
            }
            val menuClose = MenuItem("删除")
            menuClose.onAction = EventHandler {
                TaskHandler<Int>()
                        .whenCall { RtpLinkListDao().delById(vo.id!!) }
                        .andThen {
                            val flowPane = parent as FlowPane
                            flowPane.children.remove(this@OpenerBtn)
                        }
                        .handle()
            }
            val cMenu = ContextMenu(menuClose)
            contextMenu = cMenu
        }

        fun getLinkString(): String? {
            return vo.link
        }

        override fun equals(other: Any?): Boolean {
            if (other !is OpenerBtn) {
                return false
            }
            return vo.link == other.vo.link
        }

        override fun hashCode(): Int {
            return vo.link.hashCode()
        }
    }

    companion object {
        @Volatile
        var instance: RecentTouchPane? = null
            get() {
                if (field == null) {
                    // 同步代码块
                    synchronized(RecentTouchPane::class.java) {
                        if (field == null) {
                            field = WeakReference(RecentTouchPane()).get()
                        }
                    }
                }
                return field
            }
    }

    //私有构造器
    init {
        initView()
    }

}