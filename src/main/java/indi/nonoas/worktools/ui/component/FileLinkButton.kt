package indi.nonoas.worktools.ui.component

import javafx.scene.control.Button
import javafx.scene.control.ContentDisplay

/**
 *
 * @version
 * @since
 * @author Nonoas
 * @date 2024/6/2
 */
open class FileLinkButton : Button() {
    companion object{
        private const val SIZE = 90.0
    }

    init {
        contentDisplay = ContentDisplay.TOP
        prefWidth = SIZE
        prefHeight = SIZE
        maxWidth = SIZE
        maxHeight = SIZE
    }
}