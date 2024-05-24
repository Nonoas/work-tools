package indi.nonoas.worktools.view.env

import indi.nonoas.worktools.common.CommonInsets
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.Button
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox

class JdkVersionPane : BorderPane() {

    private val jdkVersions: ObservableList<JdkVersion> = FXCollections.observableArrayList()

    init {


        val tableView = TableView<JdkVersion>()
        val nameColumn = TableColumn<JdkVersion, String>("Name")
        val pathColumn = TableColumn<JdkVersion, String>("Path")

        nameColumn.cellValueFactory = PropertyValueFactory("name")
        pathColumn.cellValueFactory = PropertyValueFactory("path")

        tableView.columns.addAll(nameColumn, pathColumn)
        tableView.items = jdkVersions

        val nameField = TextField()
        nameField.promptText = "JDK Name"

        val pathField = TextField()
        pathField.promptText = "JDK Path"

        val addButton = Button("新增")
        addButton.setOnAction {
            val name = nameField.text
            val path = pathField.text
            if (name.isNotEmpty() && path.isNotEmpty()) {
                jdkVersions.add(JdkVersion(name, path))
                nameField.clear()
                pathField.clear()
            }
        }

        val deleteButton = Button("删除")
        deleteButton.setOnAction {
            val selectedJdk = tableView.selectionModel.selectedItem
            selectedJdk?.let { jdkVersions.remove(it) }
        }

        val editButton = Button("修改")
        editButton.setOnAction {
            val selectedJdk = tableView.selectionModel.selectedItem
            selectedJdk?.run {
                val editedJdk = JdkVersion(nameField.text, pathField.text)
                val index = jdkVersions.indexOf(selectedJdk)
                jdkVersions[index] = editedJdk
                nameField.clear()
                pathField.clear()
            }
        }

        val vbox = VBox(CommonInsets.SPACING_1, nameField, pathField, addButton, editButton, deleteButton)
        center = HBox(CommonInsets.SPACING_1, tableView, vbox)
    }
}