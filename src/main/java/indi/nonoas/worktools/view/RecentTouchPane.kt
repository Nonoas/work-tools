package indi.nonoas.worktools.view

import indi.nonoas.worktools.common.CommonInsets
import indi.nonoas.worktools.ui.TaskHandler
import indi.nonoas.worktools.ui.UIFactory
import indi.nonoas.worktools.ui.component.ExceptionAlter
import indi.nonoas.worktools.ui.component.MyAlert
import indi.nonoas.worktools.utils.DBUtil
import indi.nonoas.worktools.utils.UIUtil
import javafx.collections.ObservableList
import javafx.embed.swing.SwingNode
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
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.sql.ResultSet
import java.sql.SQLException
import java.util.function.Consumer
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.SwingConstants
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
    private val btnSave = UIFactory.getPrimaryButton("保存")

    private fun initView() {
        alignment = Pos.BOTTOM_RIGHT
        padding = CommonInsets.PADDING_20
        flowPane.apply {
            onDragOver = dragOverHandler
            onDragDropped = dragDropHandler
        }
        btnSave.onAction = EventHandler {
            saveToDB()
        }
        children.addAll(flowPane, btnSave)
        setVgrow(flowPane, Priority.ALWAYS)
        initFromDB()
    }

    private fun initFromDB() {
        TaskHandler<ResultSet>()
                .whenCall {
                    val conn = DBUtil.getConnection()
                    val psInit = conn.prepareStatement("select LINK from RTP_LINKLIST")
                    psInit.executeQuery()
                }
                .andThen(Consumer { rs ->
                    while (rs.next()) {
                        val link = rs.getString("link")
                        val file = File(link)
                        if (file.exists()) {
                            openerBtnList.add(OpenerBtn(file))
                        }
                    }
                }).handle()
    }

    /**
     * 保存当前布局到数据库
     */
    private fun saveToDB() {
        TaskHandler<Any>()
                .whenCall {
                    val conn = DBUtil.getConnection()
                    val psDel = conn.prepareStatement("delete from RTP_LINKLIST where 1=1")
                    val psAdd = conn.prepareStatement("insert into RTP_LINKLIST(name, link) values (?,? )")
                    psDel?.executeUpdate()

                    for (btn in openerBtnList) {
                        val openerBtn = btn as OpenerBtn
                        psAdd.setString(1, btn.text)
                        psAdd.setString(2, openerBtn.getLinkString())
                        psAdd.addBatch()
                    }
                    try {
                        val array = psAdd.executeBatch()
                        return@whenCall array
                    } catch (e: SQLException) {
                        ExceptionAlter(e).show()
                    } finally {
                        psAdd.close()
                        psDel.close()
                    }
                }
                .andThen {
                    MyAlert(AlertType.INFORMATION, "保存成功").show()
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
        if (dragboard.hasFiles()) {
            try {
                val files = dragboard.files
                for (f in files) {
                    addOpenerBtn(OpenerBtn(f))
                }
            } catch (e: Exception) {
                ExceptionAlter(e).showAndWait()
            }
        }
    }

    private fun addOpenerBtn(btn: OpenerBtn): Boolean {
        for (node in openerBtnList) {
            if (node == btn) {
                return false
            }
        }
        openerBtnList.add(btn)
        return true
    }

    private class OpenerBtn(private val file: File) : Button(file.name) {

        init {
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
                } catch (e: IOException) {
                    MyAlert(AlertType.ERROR, "文件打开失败！").show()
                }
            }
            val menuClose = MenuItem("删除")
            menuClose.onAction =
                    EventHandler { (parent as FlowPane).children.remove(this@OpenerBtn) }
            val cMenu = ContextMenu(menuClose)
            contextMenu = cMenu
        }

        fun getLinkString(): String {
            return file.absolutePath
        }

        override fun equals(other: Any?): Boolean {
            if (other !is OpenerBtn) {
                return false
            }
            return file.absolutePath == other.file.absolutePath
        }

        override fun hashCode(): Int {
            return file.absolutePath.hashCode()
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