package indi.nonoas.worktools.utils

import indi.nonoas.worktools.ui.component.ExceptionAlter
import java.awt.Desktop
import java.io.File

/**
 * @author Nonoas
 * @datetime 2022/5/15 22:03
 */
object DesktopUtil {
    @JvmStatic
    fun open(file: File?) {
        try {
            Desktop.getDesktop().open(file)
        } catch (e: Exception) {
            ExceptionAlter.error(e)
        }
    }
}
