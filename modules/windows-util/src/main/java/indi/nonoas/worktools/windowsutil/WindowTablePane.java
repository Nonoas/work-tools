package indi.nonoas.worktools.windowsutil;


import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import github.nonoas.jfx.flat.ui.control.Switch;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * @author huangshengsheng
 * @date 2025/12/3 22:29
 */
public class WindowTablePane extends VBox {

    private final ObservableList<WindowInfo> windowList = FXCollections.observableArrayList();

    private static final WindowTablePane windowTablePane = new WindowTablePane();

    public static WindowTablePane getInstance() {
        return windowTablePane;
    }

    private WindowTablePane() {
        setSpacing(5);
        setPadding(new Insets(5));

        // 标题列
        TableColumn<WindowInfo, String> titleCol = new TableColumn<>("窗口标题");
        titleCol.setCellValueFactory(data -> data.getValue().titleProperty());
        titleCol.setPrefWidth(400);

        // 置顶按钮列
        TableColumn<WindowInfo, Boolean> topMostCol = new TableColumn<>("置顶");
        topMostCol.setResizable(false);
        topMostCol.setCellValueFactory(data -> data.getValue().topMostProperty());
        topMostCol.setCellFactory(col -> new TableCell<>() {
            private final Switch switchBtn = new Switch();

            private final ChangeListener<Boolean> listener = (ov, oldVal, newVal) -> {
                WindowInfo info = getTableView().getItems().get(getIndex());
                if (newVal) {
                    WindowUtils.setTopMost(info.getHwnd());
                } else {
                    WindowUtils.removeTopMost(info.getHwnd());
                }
            };

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    switchBtn.selectedProperty().removeListener(listener);
                    setGraphic(null);
                } else {
                    // 更新现有实例的状态
                    switchBtn.setSelected(item);
                    switchBtn.selectedProperty().addListener(listener);
                    setGraphic(switchBtn);
                }
            }
        });

        TableView<WindowInfo> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.getColumns().addAll(titleCol, topMostCol);
        table.setItems(windowList);

        VBox.setVgrow(table, Priority.ALWAYS);

        Button refreshBtn = new Button("刷新列表");
        refreshBtn.setOnAction(e -> refreshWindowList());

        getChildren().addAll(refreshBtn, table);

        refreshWindowList();
    }

    public void refreshWindowList() {
        windowList.clear();
        List<HWND> hwnds = getAllWindows();
        for (HWND hwnd : hwnds) {
            String title = WindowUtils.getWindowTitle(hwnd);
            if (!title.isEmpty()) {
                windowList.add(new WindowInfo(hwnd, title));
            }
        }
    }

    private List<HWND> getAllWindows() {
        List<HWND> list = new ArrayList<>();
        User32.INSTANCE.EnumWindows((hwnd, data) -> {
            if (User32.INSTANCE.IsWindowVisible(hwnd)) {
                list.add(hwnd);
            }
            return true;
        }, null);
        return list;
    }
}
