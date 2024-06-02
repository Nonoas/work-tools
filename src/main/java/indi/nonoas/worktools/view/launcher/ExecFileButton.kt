package indi.nonoas.worktools.view.launcher

import indi.nonoas.worktools.pojo.vo.ExecFileVo
import indi.nonoas.worktools.ui.component.FileLinkButton
import indi.nonoas.worktools.utils.DesktopUtil
import indi.nonoas.worktools.utils.UIUtil
import javafx.event.EventHandler
import javafx.scene.control.Tooltip
import javafx.scene.image.ImageView
import java.io.File

/**
 *
 * @version 1.0
 * @since 1.3.2
 * @author Nonoas
 * @date 2024/6/2
 */
class ExecFileButton : FileLinkButton {

    constructor()

    constructor(vo: ExecFileVo) {
        text = vo.name
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
    }
}