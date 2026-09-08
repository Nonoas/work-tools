package indi.nonoas.worktools.recenttouch

import io.github.nonoas.worktools.platform.common.CommonInsets
import io.github.nonoas.worktools.platform.dao.RtpLinkListDao
import io.github.nonoas.worktools.platform.ext.FuncPane
import io.github.nonoas.worktools.platform.global.message.MessageBus
import io.github.nonoas.worktools.platform.global.message.MsgBusManager
import io.github.nonoas.worktools.platform.pojo.po.RtpLinkListPo
import io.github.nonoas.worktools.platform.pojo.vo.RtpLinkListVo
import io.github.nonoas.worktools.platform.ui.TaskHandler
import io.github.nonoas.worktools.platform.ui.component.ExceptionAlter
import io.github.nonoas.worktools.platform.ui.component.SearchListener
import io.github.nonoas.worktools.platform.utils.UIUtil
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.Parent
import javafx.scene.control.ContextMenu
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.MenuItem
import javafx.scene.control.TextInputDialog
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
import java.util.Locale

/**
 * 临时存放最近使用的文件，方便快捷打开
 *
 * @author Nonoas
 * @date 2021/9/5
 */
class RecentTouchPane private constructor() : FuncPane() {

    private val root = VBox(10.0)
    private val allItems = ArrayList<RtpLinkListVo>()
    private var currentKeyword = ""
    private var msgBusConnection: MessageBus.Connection? = null

    private val lv = ListView<RtpLinkListVo>().apply {
        setCellFactory { lv ->
            val cell = object : ListCell<RtpLinkListVo>() {

                init {
                    onMouseClicked = EventHandler { event ->
                        if (event.clickCount == 2 && event.button == MouseButton.PRIMARY) {
                            val item = item ?: return@EventHandler

                            Desktop.getDesktop().open(File(item.link))
                        items.remove(item)
                        allItems.remove(item)
                        allItems.add(0, item)
                        refreshView()

                        item.lastUseTimestamp = System.currentTimeMillis()
                        TaskHandler.Companion.backRun { RtpLinkListDao.replace(item) }
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
                    val nameLabel = Label(item.name).apply {
                        minWidth = USE_PREF_SIZE
                    }

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
                    val menuRename = MenuItem("重命名")
                    menuRename.onAction = EventHandler {
                        showRenameDialog(item)
                    }
                    val menuDel = MenuItem("删除")
                    menuDel.onAction = EventHandler {
                        allItems.remove(item)
                        refreshView()
                        TaskHandler.Companion.backRun { RtpLinkListDao.delById(item.id!!) }
                    }
                    contextMenu = ContextMenu(menuRename, menuDel)
                }

            }
            cell.prefWidthProperty().bind(lv.widthProperty().subtract(5)); // 0 是为了微调，有时需要减去滚动条的宽度
            cell
        }
    }

    private val logger: Logger = LogManager.getLogger(RecentTouchPane::class.java)

    private fun initFromDB() {
        allItems.clear()
        TaskHandler<MutableList<RtpLinkListPo>>()
            .whenCall { RtpLinkListDao.getAll() }
            .andThen { pos ->
                for (po in pos) {
                    allItems.add(po.covertVo())
                }
                refreshView()
            }.handle()
    }

    private fun refreshView() {
        val keyword = currentKeyword.trim().lowercase(Locale.getDefault())
        if (keyword.isBlank()) {
            lv.items.setAll(allItems)
            return
        }

        lv.items.setAll(
            allItems.filter { vo ->
                vo.name.orEmpty().lowercase(Locale.getDefault()).contains(keyword)
                        || vo.link.lowercase(Locale.getDefault()).contains(keyword)
            }
        )
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
        for (item in allItems) {
            if (vo.link == item.link) {
                return false
            }
        }
        allItems.add(vo)
        refreshView()
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

    private fun showRenameDialog(item: RtpLinkListVo) {
        val oldName = item.name?.trim().orEmpty()
        val dialog = TextInputDialog(oldName).apply {
            title = "重命名"
            headerText = "请输入新的展示名称"
            contentText = "名称:"
            lv.scene?.window?.let { initOwner(it) }
        }

        dialog.showAndWait().ifPresent { input ->
            val newName = input.trim()
            if (newName.isEmpty() || newName == oldName) {
                return@ifPresent
            }

            item.name = newName
            refreshView()
            TaskHandler.Companion.backRun { RtpLinkListDao.replace(item) }
        }
    }

    private fun initSearchListener() {
        msgBusConnection?.dispose()
        msgBusConnection = MsgBusManager.getCurrentBus().connect(this).apply {
            subscribe(SearchListener.TOPIC, object : SearchListener {
                override fun onTextChange(keyword: String) {
                    currentKeyword = keyword
                    refreshView()
                }

                override fun onEntered(event: javafx.event.ActionEvent) {
                }
            })
        }
    }

    companion object {
        @JvmStatic
        val instance: RecentTouchPane by lazy { RecentTouchPane() }
    }

    override fun getRootView(): Parent {
        root.alignment = Pos.BOTTOM_RIGHT
        root.padding = CommonInsets.PADDING_20
        lv.apply {
            onDragOver = dragOverHandler
            onDragDropped = dragDropHandler
        }
        root.children.setAll(lv)
        VBox.setVgrow(lv, Priority.ALWAYS)
        initFromDB()
        initSearchListener()
        return root
    }

    override fun dispose() {
        msgBusConnection?.dispose()
        msgBusConnection = null
        lv.items.clear()
        allItems.clear()
        currentKeyword = ""
        root.children.clear()
    }

}
