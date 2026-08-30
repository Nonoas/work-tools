package io.github.nonoas.worktools.platform.ui.component

import github.nonoas.jfx.flat.ui.theme.Styles
import io.github.nonoas.worktools.platform.dao.ExecFileDao
import io.github.nonoas.worktools.platform.pojo.vo.ExecFileVo
import io.github.nonoas.worktools.platform.ui.TaskHandler
import io.github.nonoas.worktools.platform.utils.DesktopUtil
import io.github.nonoas.worktools.platform.utils.UIUtil
import javafx.event.EventHandler
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.TextInputDialog
import javafx.scene.control.Tooltip
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import java.io.File

/**
 *
 * @version 1.0
 * @since 1.3.2
 * @author Nonoas
 * @date 2024/6/2
 */
class ExecFileButton : FileLinkButton {

    constructor(){
        styleClass.addAll(Styles.SMALL)
    }

    constructor(vo: ExecFileVo) : this() {
        updateDisplayName(vo.name)
        graphic = ImageView(UIUtil.getFileIcon(vo.link))
        onAction = EventHandler {
            val file = File(vo.link)
            if (!file.exists()) {
                UIUtil.error("文件${vo.link}不存在")
                return@EventHandler
            }
            DesktopUtil.open(file)
        }

        val miRename = MenuItem("重命名")
        miRename.onAction = EventHandler {
            showRenameDialog(vo)
        }
        val miReveal = MenuItem("打开所在位置")
        miReveal.onAction = EventHandler {
            val file = File(vo.link)
            val parent = file.parentFile
            if (!file.exists() && (parent == null || !parent.exists())) {
                UIUtil.error("文件${vo.link}不存在")
                return@EventHandler
            }
            DesktopUtil.revealInFileManager(file)
        }
        val miDel = MenuItem("删除")
        miDel.onAction= EventHandler {
            (parent as Pane).children.remove(this)
            ExecFileDao.delByUniqueKey(vo)
        }
        val ctMenu = ContextMenu(miReveal, miRename, miDel)
        contextMenu = ctMenu
    }

    private fun showRenameDialog(vo: ExecFileVo) {
        val oldName = vo.name?.trim().orEmpty()
        val dialog = TextInputDialog(oldName).apply {
            title = "重命名"
            headerText = "请输入新的展示名称"
            contentText = "名称:"
            this@ExecFileButton.scene?.window?.let { initOwner(it) }
        }

        dialog.showAndWait().ifPresent { input ->
            val newName = input.trim()
            if (newName.isEmpty() || newName == oldName) {
                return@ifPresent
            }

            vo.name = newName
            updateDisplayName(newName)
            TaskHandler.backRun { ExecFileDao.updateNameByUniqueKey(vo, newName) }
        }
    }

    private fun updateDisplayName(name: String?) {
        text = name?.substringBeforeLast('.')
        tooltip = Tooltip(text)
    }
}
