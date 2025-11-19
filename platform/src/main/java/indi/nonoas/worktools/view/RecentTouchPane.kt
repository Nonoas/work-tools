package indi.nonoas.worktools.view

import indi.nonoas.worktools.common.CommonInsets
import indi.nonoas.worktools.dao.RtpLinkListDao
import indi.nonoas.worktools.pojo.po.RtpLinkListPo
import indi.nonoas.worktools.pojo.vo.RtpLinkListVo
import indi.nonoas.worktools.ui.TaskHandler
import indi.nonoas.worktools.ui.component.ExceptionAlter
import indi.nonoas.worktools.utils.UIUtil
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.ContextMenu
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.MenuItem
import javafx.scene.control.Tooltip
import javafx.scene.image.ImageView
import javafx.scene.input.DragEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.TransferMode
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.awt.Desktop
import java.io.File
import java.lang.ref.WeakReference


/**
 * 临时存放最近使用的文件，方便快捷打开
 *
 * @author Nonoas
 * @date 2021/9/5
 */
class RecentTouchPane private constructor() : VBox(10.0) {

    private val lv = ListView<RtpLinkListVo>().apply {
        setCellFactory { lv ->
            val cell = object : ListCell<RtpLinkListVo>() {

                init {
                    onMouseClicked = EventHandler { event ->
                        if (event.clickCount == 2 && event.button == MouseButton.PRIMARY) {
                            val item = item ?: return@EventHandler

                            Desktop.getDesktop().open(File(item.link))
                            items.remove(item)
                            items.add(0, item)

                            item.lastUseTimestamp = System.currentTimeMillis()
                            TaskHandler.backRun { RtpLinkListDao.replace(item) }
                        }
                    }
                }

                override fun updateItem(item: RtpLinkListVo?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (empty || item == null) {
                        graphic = null
                        text = null
                        return
                    }

                    val file = File(item.link)
                    val fxImage = UIUtil.getFileIcon(file, 16)

                    // 文件名 Label
                    val nameLabel = Label(item.name)

                    // 路径 Label（灰色，截断显示）
                    val pathLabel = Label(file.absolutePath).apply {
                        style = "-fx-text-fill: #A0A0A0;"
                        isWrapText = false
                        ellipsisString = "..."  // JavaFX 9+ 有效；老版本可以用 CSS
                        minWidth = 0.0
                    }

                    // Tooltip 显示完整路径
                    val tooltip = Tooltip(file.absolutePath)
                    Tooltip.install(pathLabel, tooltip)
                    Tooltip.install(nameLabel, tooltip)

                    HBox.setHgrow(pathLabel, Priority.ALWAYS)

                    graphic = HBox(
                        ImageView(fxImage).apply {
                            isPreserveRatio = true
                            isSmooth = true
                        },
                        nameLabel,
                        pathLabel
                    ).apply {
                        spacing = 8.0
                        alignment = Pos.CENTER_LEFT
                        maxWidth = Double.MAX_VALUE
                    }

                    // 右键菜单
                    val menuDel = MenuItem("删除")
                    menuDel.onAction = EventHandler {
                        items.remove(item)
                        TaskHandler.backRun { RtpLinkListDao.delById(item.id!!) }
                    }
                    contextMenu = ContextMenu(menuDel)
                }

            }
            cell.prefWidthProperty().bind(lv.widthProperty().subtract(5)); // 0 是为了微调，有时需要减去滚动条的宽度
            cell
        }
    }

    private val logger: Logger = LogManager.getLogger(RecentTouchPane::class.java)

    private fun initView() {
        alignment = Pos.BOTTOM_RIGHT
        padding = CommonInsets.PADDING_20
        lv.apply {
            onDragOver = dragOverHandler
            onDragDropped = dragDropHandler
        }
        children.addAll(lv)
        setVgrow(lv, Priority.ALWAYS)
        initFromDB()
    }

    private fun initFromDB() {
        TaskHandler<MutableList<RtpLinkListPo>>()
            .whenCall { RtpLinkListDao.getAll() }
            .andThen { pos ->
                for (po in pos) {
                    lv.items.add(po.covertVo())
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
                addFileLink(po)
            }
        } catch (e: Exception) {
            ExceptionAlter(e).showAndWait()
            logger.error("添加按钮出错", e)
        }
    }

    private fun addFileLink(vo: RtpLinkListVo): Boolean {
        for (item in lv.items) {
            if (vo.link == item.link) {
                return false
            }
        }
        lv.items.add(vo)
        TaskHandler<Int>()
            .whenCall {
                logger.info("添加按钮${vo.name}")
                try {
                    return@whenCall RtpLinkListDao.add(vo.covertPo())
                } catch (e: Exception) {
                    logger.error(e)
                    return@whenCall 0
                }
            }
            .andThen {
                if (0 == it) {
                    UIUtil.error("添加按钮出错")
                }
            }
            .handle()

        return true
    }

    companion object {
        @JvmStatic
        val instance: RecentTouchPane by lazy { RecentTouchPane() }
    }

    // 私有构造器
    init {
        initView()
    }

}