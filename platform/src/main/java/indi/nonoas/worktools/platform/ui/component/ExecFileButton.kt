package indi.nonoas.worktools.platform.ui.component

import github.nonoas.jfx.flat.ui.theme.Styles
import indi.nonoas.worktools.platform.dao.ExecFileDao
import indi.nonoas.worktools.platform.pojo.vo.ExecFileVo
import indi.nonoas.worktools.platform.utils.DesktopUtil
import indi.nonoas.worktools.platform.utils.UIUtil
import javafx.event.EventHandler
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
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
        text = vo.name?.substringBeforeLast('.')
        graphic = ImageView(UIUtil.getFileIcon(vo.link))
        Tooltip.install(this, Tooltip(text))
        onAction = EventHandler {
            val file = File(vo.link)
            if (!file.exists()) {
                UIUtil.error("文件${vo.link}不存在")
                return@EventHandler
            }
            DesktopUtil.open(file)
        }

        val miDel = MenuItem("删除")
        miDel.onAction= EventHandler {
            (parent as Pane).children.remove(this)
            ExecFileDao.delByUniqueKey(vo)
        }
        val ctMenu = ContextMenu(miDel)
        contextMenu = ctMenu
    }
}