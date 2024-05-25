package indi.nonoas.worktools.view.workspace

import cn.hutool.core.util.StrUtil
import cn.hutool.db.Db
import cn.hutool.db.Entity
import indi.nonoas.worktools.pojo.vo.ModifyItemVo
import indi.nonoas.worktools.ui.TaskHandler
import indi.nonoas.worktools.ui.component.ExceptionAlter.Companion.error
import indi.nonoas.worktools.ui.component.MyAlert
import indi.nonoas.worktools.utils.DBUtil
import indi.nonoas.worktools.utils.DesktopUtil.open
import indi.nonoas.worktools.utils.FileUtil.deleteFile
import indi.nonoas.worktools.view.MainStage.Companion.instance
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.io.File
import java.lang.ref.WeakReference
import java.sql.SQLException
import java.util.*

/**
 * @author Nonoas
 * @date 2022/3/18
 */
open class ModifyListView : ListView<ModifyItemVo?>() {
    private var onItemDelete: OnItemDelete? = null

    init {
        placeholder = Label("没有数据")
        setCellFactory { ModifyListCell() }
        selectionModel.selectionMode = SelectionMode.MULTIPLE
    }

    fun setOnItemDelete(onItemDelete: OnItemDelete?) {
        this.onItemDelete = onItemDelete
    }

    interface OnItemDelete {
        fun apply(cell: ListCell<ModifyItemVo?>?)
    }

    /**
     * 列表单元格
     */
    protected class ModifyListCell : ListCell<ModifyItemVo?>() {
        /**
         * 双击事件处理
         */
        private val doubleClickHandler = WeakReference(
                EventHandler { event: MouseEvent ->
                    // 鼠标左键双击事件
                    if (2 == event.clickCount && event.button == MouseButton.PRIMARY) {
                        event.consume()
                        open(
                                File(Objects.requireNonNull(item!!.absolutePath))
                        )
                    }
                }
        ).get()!!

        override fun updateItem(p0: ModifyItemVo?, empty: Boolean) {
            super.updateItem(item, empty)
            if (!empty || null != item) {
                text = item.toString()
                initContextMenu()
                removeEventHandler(MouseEvent.MOUSE_CLICKED, doubleClickHandler)
                addEventHandler(MouseEvent.MOUSE_CLICKED, doubleClickHandler)
            } else {
                // 移除监听和右键菜单
                removeEventHandler(MouseEvent.MOUSE_CLICKED, doubleClickHandler)
                val contextMenu = contextMenu
                contextMenu?.items?.clear()
                // 设置图像和文字为 null
                text = null
                graphic = null
            }
        }

        /**
         * 初始化右键菜单
         */
        private fun initContextMenu() {
            val miOpenDescFile = MenuItem("修改说明")
            miOpenDescFile.onAction = EventHandler { event: ActionEvent? -> open(File(item!!.absolutePath + File.separator + "0-修改说明.md")) }

            val mCopyNum = MenuItem("复制单号")
            mCopyNum.onAction = EventHandler { event: ActionEvent? ->
                val clip = Toolkit.getDefaultToolkit().systemClipboard
                val tText: Transferable = StringSelection(item!!.modifyNum)
                clip.setContents(tText, null)
            }


            val miRename = MenuItem("重命名")
            miRename.onAction = EventHandler {
                val item = item
                val path = item!!.absolutePath
                if (StrUtil.isBlank(path)) {
                    return@EventHandler
                }
                val dialog: TextInputDialog = object : TextInputDialog(item.desc) {
                    init {
                        headerText = null
                        initOwner(instance!!.stage)
                    }
                }
                dialog.showAndWait().ifPresent { s: String -> renameItem(item, path, s) }
            }

            val miDel = MenuItem("删除")
            miDel.onAction = EventHandler { event: ActionEvent? ->
                TaskHandler<Int?>()
                        .whenCall {
                            try {
                                val absolutePath = item!!.absolutePath
                                deleteFile(File(absolutePath!!))
                                return@whenCall DBUtil.use().del("modify_items", "modify_num", item!!.modifyNum)
                            } catch (e: SQLException) {
                                error(e)
                            }
                            null
                        }.andThen { integer: Int? ->
                            val lv = listView as ModifyListView
                            if (lv.onItemDelete != null) {
                                lv.onItemDelete!!.apply(this@ModifyListCell)
                            }
                        }.handle()
            }
            val menu = ContextMenu(miOpenDescFile, mCopyNum, miRename, miDel)
            contextMenu = menu
        }

        private fun renameItem(item: ModifyItemVo?, path: String?, desc: String) {
            val newName = String.format("%s-%s-%s", item!!.modifyNum, desc, item.versionNO)
            val newPath = path!!.substring(0, path.lastIndexOf(File.separator) + 1) + newName
            val b = File(path).renameTo(File(newPath))
            if (!b) {
                MyAlert(Alert.AlertType.ERROR, "重命名失败").showAndWait()
            } else {
                item.setModifyReason(desc)
                item.desc = desc
                item.absolutePath = newPath
                updateToDB(item)
                // 更新当前 item 内容
                updateItem(item, false)
            }
        }

        private fun updateToDB(item: ModifyItemVo?) {
            val po = item!!.convertPo()
            po.modifyTime = System.currentTimeMillis()
            val entity = Entity.create("modify_items")
                    .set("absolute_path", po.absolutePath)
                    .set("modify_time", System.currentTimeMillis())
                    .set("desc", po.desc)
            val where = Entity.create(entity.tableName).set("modify_num", po.modifyNum)
            try {
                DBUtil.use().update(entity, where)
            } catch (e: SQLException) {
                error(e)
            }
        }
    }
}
