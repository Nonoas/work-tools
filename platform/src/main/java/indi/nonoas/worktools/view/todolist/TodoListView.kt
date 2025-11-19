import indi.nonoas.worktools.view.todolist.TodoListVo
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.layout.*
import javafx.util.Callback
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material2.Material2AL
import org.kordamp.ikonli.material2.Material2SharpMZ

class TodoListView : ListView<TodoListVo>() {

    init {
        style = """
             -fx-background-insets: 0;
             -fx-border-width: 0;
             -fx-background-color: transparent;
        """.trimIndent()

        cellFactory = Callback {
            TodoListCell()
        }
    }

    // 新增待办项
    fun addNewItem() {
        val newItem = TodoListVo("")
        items.add(newItem)
        selectionModel.select(newItem) // 选中新建项，自动进入编辑
    }

    private inner class TodoListCell : ListCell<TodoListVo>() {

        init {
            padding = Insets.EMPTY
        }

        override fun updateItem(item: TodoListVo?, empty: Boolean) {
            super.updateItem(item, empty)
            if (item == null || empty) {
                graphic = null
            } else {
                val isEditing = item.toString().isBlank() || item == listView.selectionModel.selectedItem
                createGraphic(item, isEditing)
            }
        }

        private fun createGraphic(item: TodoListVo, isEditing: Boolean) {
            val checkBox = CheckBox().apply {
                selectedProperty().addListener { _, _, newVal ->
                    if (newVal) listView.items.remove(item)
                }
            }

            val textField = TextField(item.toString())

            val editButton = Button("编辑").apply {
                setOnAction {
                    listView.selectionModel.select(item)
                    textField.isEditable = true
                }
            }

            val upButton = Button(null, FontIcon(Material2AL.ARROW_UPWARD)).apply {
                setOnAction {
                    val idx = listView.items.indexOf(item)
                    if (idx > 0) {
                        listView.items.swap(idx, idx - 1)
                    }
                }
            }

            val downButton = Button(null, FontIcon(Material2AL.ARROW_DOWNWARD)).apply {
                setOnAction {
                    val idx = listView.items.indexOf(item)
                    if (idx < listView.items.size - 1) {
                        listView.items.swap(idx, idx + 1)
                    }
                }
            }

            val topButton = Button(null, FontIcon(Material2SharpMZ.VERTICAL_ALIGN_TOP)).apply {
                setOnAction {
                    val idx = listView.items.indexOf(item)
                    if (idx > 0) {
                        listView.items.removeAt(idx)
                        listView.items.add(0, item)
                    }
                }
            }

            val buttonBox = HBox(5.0, upButton, downButton, topButton, editButton).apply {
                alignment = Pos.CENTER_RIGHT
            }

            val hBox = HBox(10.0, checkBox, textField, buttonBox).apply {
                alignment = Pos.CENTER_LEFT
                padding = Insets(10.0)
                style = """
                      -fx-effect: dropshadow(two-pass-box, rgba(0, 0, 0, 0.4), 2, 0.0, 0, 0);
                      -fx-background-color: white;
                    """.trimIndent()
                HBox.setHgrow(buttonBox, Priority.ALWAYS)
            }

            if (isEditing) {
                textField.setOnAction {
                    saveEdit(item, textField.text)
                }
                textField.setOnKeyPressed {
                    if (it.code == KeyCode.ESCAPE) {
                        listView.selectionModel.clearSelection()
                        refresh()
                    }
                }
                textField.focusedProperty().addListener { _, _, focused ->
                    if (!focused) {
                        saveEdit(item, textField.text)
                    }
                }
                textField.sceneProperty().addListener { _, _, newScene ->
                    if (newScene != null) {
                        Platform.runLater {
                            textField.requestFocus()
                            textField.positionCaret(textField.text.length)
                        }
                    }
                }
            }

            graphic = hBox
        }

        private fun saveEdit(item: TodoListVo, newText: String) {
            val idx = listView.items.indexOf(item)
            if (idx >= 0) {
                val updatedItem = TodoListVo(newText)
                listView.items[idx] = updatedItem
                listView.selectionModel.clearSelection()
            }
        }
    }

    // Kotlin扩展函数：交换列表元素
    private fun <T> MutableList<T>.swap(i: Int, j: Int) {
        val tmp = this[i]
        this[i] = this[j]
        this[j] = tmp
    }
}
