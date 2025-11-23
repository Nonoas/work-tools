package indi.nonoas.worktools.platform.ui.component

import javafx.collections.ObservableList
import javafx.scene.control.ComboBox
import javafx.scene.control.ListView
import javafx.scene.input.MouseEvent
import javafx.util.Callback

/**
 * 选项可关闭的下拉列表框
 *
 * @author Nonoas
 * @date 2022/1/6
 */
class ItemCloseableComboBox<T> : ComboBox<T> {
    private var onItemClosed: CloseableListCell.OnClosed? = null

    constructor() {
        init()
    }

    constructor(items: ObservableList<T>?) : super(items) {
        init()
    }

    private fun init() {
        cellFactory = Callback { lv: ListView<T> ->
            lv.prefWidthProperty().bind(widthProperty())
            val listCell = CloseableListCell<T?>()
            listCell.setOnClosed { event: MouseEvent?, listView: ListView<*>?, index: Int ->
                if (onItemClosed != null) {
                    onItemClosed!!.apply(event, listView, index)
                }
            }
            listCell.setStringConvert(getConverter())
            listCell
        }
    }

    fun setOnItemClosed(onItemClosed: CloseableListCell.OnClosed?) {
        this.onItemClosed = onItemClosed
    }
}
