package indi.nonoas.worktools.windowsutil

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.HWND
import github.nonoas.jfx.flat.ui.concurrent.TaskHandler
import github.nonoas.jfx.flat.ui.control.Switch
import io.github.nonoas.worktools.platform.ext.FuncPane
import io.github.nonoas.worktools.platform.global.message.MsgBusManager
import io.github.nonoas.worktools.platform.ui.component.SearchListener
import io.github.nonoas.worktools.platform.ui.component.SearchListener.Companion.TOPIC
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.embed.swing.SwingFXUtils
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Parent
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
import java.util.stream.Collectors

/**
 * @author huangshengsheng
 * @date 2025/12/3 22:29
 */
class WindowTablePane private constructor() : FuncPane() {
    private val windowList: ObservableList<WindowInfo> = FXCollections.observableArrayList()

    private val table = TableView<WindowInfo>()

    private val root = VBox()

    private val refreshBtn = Button("刷新列表")

    private var viewInitialized = false

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
                    val imageView = if (windowIcon != null) {
                        ImageView(SwingFXUtils.toFXImage(windowIcon, null))
                    } else {
                        ImageView()
                    }
                    imageView.fitWidth = 16.0
                    imageView.fitHeight = 16.0
                    tmpList.add(WindowInfo(dw.hwnd, title, imageView))
                }
            }
            tmpList
        }
            .andThen { windows ->
                windowList.addAll(windows!!)
            }
            .handle()
    }

    fun queryFilter(keyword: String?) {
        if (keyword.isNullOrBlank()) {
            table.items = windowList
            return
        }
        val collect = windowList.stream()
            .filter { windowInfo ->
                windowInfo.title.uppercase(Locale.getDefault())
                    .contains(keyword.uppercase(Locale.getDefault()))
            }
            .collect(Collectors.toCollection { FXCollections.observableArrayList() })
        table.items = collect
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

    private fun initView() {
        if (viewInitialized) {
            return
        }

        root.spacing = 5.0
        root.padding = Insets(5.0)

        val titleCol = TableColumn<WindowInfo, String>("窗口标题")
        titleCol.setCellValueFactory { data -> data.value.titleProperty() }
        titleCol.setCellFactory {
            object : TableCell<WindowInfo?, String?>() {
                override fun updateItem(item: String?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (empty || item == null) {
                        graphic = null
                        return
                    }

                    try {
                        val info = tableView.items[index]!!
                        val hBox = HBox(10.0, info.icon, Label(info.title))
                        hBox.alignment = Pos.CENTER_LEFT
                        graphic = hBox
                    } catch (_: IndexOutOfBoundsException) {
                        graphic = null
                    } catch (e: Exception) {
                        System.err.println("Error rendering TableCell: ${e.message}")
                        graphic = Label("渲染错误")
                    }
                }
            }
        }
        titleCol.prefWidth = 400.0

        val topMostCol = TableColumn<WindowInfo, Boolean>("置顶")
        topMostCol.isResizable = false
        topMostCol.setCellValueFactory { data -> data.value.topMostProperty() }
        topMostCol.setCellFactory {
            object : TableCell<WindowInfo?, Boolean?>() {
                private val switchBtn = Switch()

                private val listener =
                    ChangeListener { _: ObservableValue<out Boolean>?, _: Boolean?, newVal: Boolean ->
                        val info = tableView.items[index]!!
                        if (newVal) {
                            WindowUtils.setTopMost(info.hwnd)
                        } else {
                            WindowUtils.removeTopMost(info.hwnd)
                        }
                    }

                override fun updateItem(item: Boolean?, empty: Boolean) {
                    super.updateItem(item, empty)
                    switchBtn.selectedProperty().removeListener(listener)
                    if (empty || item == null) {
                        graphic = null
                        return
                    }

                    switchBtn.isSelected = item
                    switchBtn.selectedProperty().addListener(listener)
                    graphic = switchBtn
                }
            }
        }

        table.columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS
        table.columns.setAll(titleCol, topMostCol)
        table.items = windowList

        VBox.setVgrow(table, Priority.ALWAYS)

        refreshBtn.onAction = EventHandler { _: ActionEvent? -> refreshWindowList() }
        root.children.setAll(refreshBtn, table)

        viewInitialized = true
    }

    override fun getRootView(): Parent {
        initView()
        root.children.setAll(refreshBtn, table)
        table.items = windowList
        refreshWindowList()

        MsgBusManager.getCurrentBus()
            .connect(this)
            .subscribe(TOPIC, object : SearchListener {
                override fun onTextChange(keyword: String) {
                    queryFilter(keyword)
                }

                override fun onEntered(event: KeyEvent) {
                }
            })
        return root
    }

    override fun dispose() {
        table.items = FXCollections.observableArrayList()
        root.children.clear()
    }
}
