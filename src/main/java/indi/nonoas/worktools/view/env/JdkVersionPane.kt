package indi.nonoas.worktools.view.env

import atlantafx.base.theme.Styles
import cn.hutool.core.io.FileUtil
import indi.nonoas.worktools.common.CommonInsets
import indi.nonoas.worktools.ui.TaskHandler
import indi.nonoas.worktools.utils.DesktopUtil
import indi.nonoas.worktools.utils.UIUtil
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.Button
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TextField
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import java.io.File

class JdkVersionPane : HBox(CommonInsets.SPACING_1) {

    private val JAVA_HOME = "JAVA_HOME"

    private val nameField = TextField().apply { promptText = "JDK 名称" }

    private val pathField = TextField().apply { promptText = "JDK 路径" }

    private val jdkVersions: ObservableList<EnvVar> = FXCollections.observableArrayList()

    init {
        padding = CommonInsets.PADDING_20

        val tableView = TableView<EnvVar>()
        val nameColumn = TableColumn<EnvVar, String>("名称")
        val pathColumn = TableColumn<EnvVar, String>("路径")

        nameColumn.cellValueFactory = PropertyValueFactory("desc")
        pathColumn.cellValueFactory = PropertyValueFactory("content")

        tableView.columns.addAll(nameColumn, pathColumn)
        tableView.items = jdkVersions

        setHgrow(tableView, Priority.ALWAYS)

        val addButton = Button("新增")
        addButton.setOnAction { onAdd() }

        val deleteButton = Button("删除")
        deleteButton.setOnAction {
            val selectedJdk = tableView.selectionModel.selectedItem
            selectedJdk?.let { jdkVersions.remove(it) }
        }

        val editButton = Button("修改")
        editButton.setOnAction {
            val selectedJdk = tableView.selectionModel.selectedItem
            selectedJdk?.run {
                val editedJdk = EnvVar().apply {
                    name = JAVA_HOME
                    desc = nameField.text
                    content = pathField.text
                    createTimestamp = System.currentTimeMillis()
                }
                val index = jdkVersions.indexOf(selectedJdk)
                jdkVersions[index] = editedJdk
                clearForm(nameField, pathField)
            }
        }

        val enable = Button("启用").apply {
            styleClass.add(Styles.ACCENT)
        }
        enable.setOnAction {
            val selectedJdk = tableView.selectionModel.selectedItem
            TaskHandler<Unit>()
                    .whenCall{ DesktopUtil.changeEnvVar(JAVA_HOME, selectedJdk.content!!)}
                    .andThen {}
                    .handle()
        }

        val vbox = VBox(CommonInsets.SPACING_1,
                nameField, pathField, addButton, editButton, deleteButton, enable)
        vbox.isFillWidth = true
        children.addAll(tableView, vbox)

        val vars = EnvVarDao.queryByName(JAVA_HOME)
        jdkVersions.addAll(vars)
    }

    private fun onAdd() {
        val name = nameField.text
        val path = pathField.text
        if (name.isBlank() || path.isBlank()) {
            return
        }

        if (!FileUtil.exist(path)) {
            UIUtil.error("${path}路径不存在")
            return
        }

        jdkVersions.add(EnvVar().apply {
            this.name = JAVA_HOME
            content = path
            desc = name
            createTimestamp = System.currentTimeMillis()
        })
        EnvVarDao.insert(
                EnvVar().apply {
                    this.name = JAVA_HOME
                    content = path
                    desc = name
                    createTimestamp = System.currentTimeMillis()
                }
        )
        clearForm(nameField, pathField)
    }

    private fun clearForm(nameField: TextField, pathField: TextField) {
        nameField.clear()
        pathField.clear()
    }
}