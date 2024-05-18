package indi.nonoas.worktools.view.todolist

import indi.nonoas.worktools.common.CommonInsets
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.layout.HBox
import javafx.util.Callback

/**
 * @author Nonoas
 * @version 1.0
 * @date 2024/5/19
 * @since 1.0
 */
class TodoListView : ListView<TodoListVo>() {
    init {
        items.addAll(TodoListVo("待办"))

        style = """
             -fx-background-insets: 0;
             -fx-border-width: 0;
             -fx-background-color: transparent;
        """.trimIndent()

        cellFactory = Callback {
            TodoListCell()
        }
    }

    private class TodoListCell : ListCell<TodoListVo>() {

        init {
            padding = Insets.EMPTY
        }
        override fun updateItem(item: TodoListVo?, empty: Boolean) {
            if (null == item || empty) {
                graphic = null
            } else {
                createGraphic(item)
            }
            super.updateItem(item, empty)
        }

        private fun createGraphic(item: TodoListVo) {
            val checkBox = CheckBox().apply {
                selectedProperty().addListener { _, _, newVal ->
                    (newVal) && listView.items.remove(item)
                }
            }
            val hBox = HBox(10.0, checkBox, Label(item.toString())).apply {
                alignment = Pos.CENTER_LEFT
                padding = CommonInsets.PADDING_10
                style = """
                      -fx-effect: dropshadow(two-pass-box, rgba(0, 0, 0, 0.4), 2, 0.0, 0, 0);
                      -fx-background-color: white;
                    """.trimIndent()
            }
            graphic = hBox
        }
    }
}
