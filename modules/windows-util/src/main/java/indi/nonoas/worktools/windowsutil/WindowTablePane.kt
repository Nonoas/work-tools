package indi.nonoas.worktools.windowsutil

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.HWND
import github.nonoas.jfx.flat.ui.concurrent.TaskHandler
import github.nonoas.jfx.flat.ui.control.Switch
import indi.nonoas.worktools.platform.global.message.MessageBus
import indi.nonoas.worktools.platform.global.message.MsgBusManager
import indi.nonoas.worktools.platform.ui.component.SearchListener
import indi.nonoas.worktools.platform.ui.component.SearchListener.Companion.TOPIC
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.embed.swing.SwingFXUtils
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.image.ImageView
import javafx.scene.input.KeyEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import java.util.Locale
import java.util.function.Supplier
import java.util.stream.Collectors


/**
 * @author huangshengsheng
 * @date 2025/12/3 22:29
 */
class WindowTablePane private constructor() : VBox() {
    private val windowList: ObservableList<WindowInfo> = FXCollections.observableArrayList()

    private val table = TableView<WindowInfo>()

    init {
        spacing = 5.0
        padding = Insets(5.0)

        // 标题列
        val titleCol = TableColumn<WindowInfo, String>("窗口标题")
        titleCol.setCellValueFactory { data: TableColumn.CellDataFeatures<WindowInfo, String> -> data.value.titleProperty() }
        titleCol.setCellFactory {
            object : TableCell<WindowInfo?, String?>() {
                override fun updateItem(item: String?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (empty || item == null) {
                        graphic = null
                    } else {
                        // 1. 尝试安全获取数据，避免异常副作用
                        try {
                            // **注意：getTableColumn() 确保我们获取的是当前列的数据模型**
                            // getIndex() 是获取当前单元格的行索引
                            val info = tableView.items[index]!!

                            // 确保 getIcon() 快速且不触发阻塞操作
                            val icon = info.icon
                            val title = info.title

                            // 2. 正常构建 UI
                            val hBox = HBox(10.0, icon, Label(title))
                            hBox.alignment = Pos.CENTER_LEFT
                            graphic = hBox
                        } catch (e: IndexOutOfBoundsException) {
                            // 预防 getIndex() 或 getItems().get() 出现问题
                            graphic = null
                        } catch (e: Exception) {
                            // 3. **关键点：如果这里出现异常，绝不能直接调用 showAndWait**
                            // 应该记录日志，或者显示一个占位符，然后稍后安全地处理异常
                            System.err.println("Error rendering TableCell: " + e.message)
                            graphic = Label("渲染错误")
                        }
                    }
                }
            }
        }
        titleCol.prefWidth = 400.0

        // 置顶按钮列
        val topMostCol = TableColumn<WindowInfo, Boolean>("置顶")
        topMostCol.isResizable = false
        topMostCol.setCellValueFactory { data: TableColumn.CellDataFeatures<WindowInfo, Boolean> -> data.value.topMostProperty() }
        topMostCol.setCellFactory { col: TableColumn<WindowInfo, Boolean>? ->
            object : TableCell<WindowInfo?, Boolean?>() {
                private val switchBtn = Switch()

                private val listener =
                    ChangeListener { ov: ObservableValue<out Boolean>?, oldVal: Boolean?, newVal: Boolean ->
                        val info = tableView.items[index]!!
                        if (newVal) {
                            WindowUtils.setTopMost(info.hwnd)
                        } else {
                            WindowUtils.removeTopMost(info.hwnd)
                        }
                    }

                override fun updateItem(item: Boolean?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (empty || item == null) {
                        switchBtn.selectedProperty().removeListener(listener)
                        graphic = null
                    } else {
                        // 更新现有实例的状态
                        switchBtn.isSelected = item
                        switchBtn.selectedProperty().addListener(listener)
                        graphic = switchBtn
                    }
                }
            }
        }

        table.columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS
        table.columns.addAll(titleCol, topMostCol)
        table.items = windowList

        setVgrow(table, Priority.ALWAYS)

        val refreshBtn = Button("刷新列表")
        refreshBtn.onAction = EventHandler { e: ActionEvent? -> refreshWindowList() }

        children.addAll(refreshBtn, table)

        refreshWindowList()

        MsgBusManager.getCurrentBus()
            .connect().subscribe(TOPIC, object : SearchListener {

            override fun onTextChange(keyword: String) {
                queryFilter(keyword)
            }

            override fun onEntered(event: KeyEvent) {
            }
        })
    }

    private fun refreshWindowList() {
        windowList.clear()
        val tmpList: MutableList<WindowInfo> = FXCollections.observableArrayList()
        TaskHandler<List<WindowInfo>>().whenCall {
            val desktopWindows = com.sun.jna.platform.WindowUtils.getAllWindows(true)
            for (dw in desktopWindows) {
                val title = dw.title
                if (title.isNotEmpty()) {
                    val hwnd = dw.hwnd
                    val windowIcon = com.sun.jna.platform.WindowUtils.getWindowIcon(hwnd)
                    var imageView: ImageView
                    if (windowIcon != null) {
                        val fxImage = SwingFXUtils.toFXImage(windowIcon, null)
                        imageView = ImageView(fxImage)
                    } else {
                        imageView = ImageView()
                    }
                    imageView.fitWidth = 16.0
                    imageView.fitHeight = 16.0
                    tmpList.add(WindowInfo(dw.hwnd, title, imageView))
                }
            }
            tmpList
        }
            .andThen { c: List<WindowInfo>? ->
                windowList.addAll(
                    c!!
                )
            }
            .handle()
    }

    fun queryFilter(keyword: String?) {
        if (keyword.isNullOrBlank()) {
            table.items = windowList
            return
        }
        val collect = windowList.stream()
            .filter { windowInfo: WindowInfo -> windowInfo.title.uppercase(Locale.getDefault()).contains(keyword.uppercase(
                Locale.getDefault()
            )) }
            .collect(
                Collectors.toCollection { FXCollections.observableArrayList() }
            )
        table.setItems(collect)
    }


    private val allWindows: List<HWND>
        get() {
            val list: MutableList<HWND> = ArrayList()
            User32.INSTANCE.EnumWindows({ hwnd: HWND, data: Pointer? ->
                if (User32.INSTANCE.IsWindowVisible(hwnd)) {
                    list.add(hwnd)
                }
                true
            }, null)
            return list
        }

    companion object {
        val instance: WindowTablePane = WindowTablePane()
    }
}
