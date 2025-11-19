package indi.nonoas.worktools.ui.component

import indi.nonoas.worktools.common.CommonInsets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.util.StringConverter

/**
 * @author Nonoas
 * @date 2022/1/6
 */
class CloseableListCell<T> : ListCell<T>() {
    private var onClosed: OnClosed? = null
    private var stringConverter: StringConverter<T>? = null
    fun setStringConvert(stringConverter: StringConverter<T>?) {
        this.stringConverter = stringConverter
    }

    override fun updateItem(item: T, empty: Boolean) {
        super.updateItem(item, empty)
        if (empty || null == item) {
            graphic = null
            return
        }
        val text = if (stringConverter != null) stringConverter!!.toString(item) else item.toString()
        val label = Label(text)
        label.maxWidth = Double.MAX_VALUE
        HBox.setHgrow(label, Priority.ALWAYS)
        val btnClose = Label("×")
        btnClose.padding = CommonInsets.PADDING_R10
        btnClose.addEventHandler(MouseEvent.MOUSE_PRESSED) { event: MouseEvent? ->
            if (null != onClosed) {
                onClosed!!.apply(event, listView, index)
            }
        }
        val hBox: HBox = HBox(label, btnClose).apply {
            alignment = Pos.CENTER_LEFT
        }
        graphic = hBox
    }

    /**
     * 设置点击关闭按钮时的事件处理类
     */
    fun setOnClosed(onClosed: OnClosed?) {
        this.onClosed = onClosed
    }

    fun interface OnClosed {
        /**
         * @param event    鼠标事件
         * @param listView 下拉列表
         * @param index    被点击的item索引
         */
        fun apply(event: MouseEvent?, listView: ListView<*>?, index: Int)
    }
}