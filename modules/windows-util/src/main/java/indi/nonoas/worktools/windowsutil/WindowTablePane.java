package indi.nonoas.worktools.windowsutil;


import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
/**
 * @author huangshengsheng
 * @date 2025/12/3 22:29
 */
public class WindowTablePane extends VBox {

    private final TableView<WindowInfo> table = new TableView<>();
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
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        titleCol.setPrefWidth(400);

        // 置顶按钮列
        TableColumn<WindowInfo, Void> topMostCol = new TableColumn<>("置顶");
        topMostCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("置顶");

            {
                btn.setOnAction(e -> {
                    WindowInfo info = getTableView().getItems().get(getIndex());
                    WindowUtils.setTopMost(info.getHwnd());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        // 取消置顶按钮列
        TableColumn<WindowInfo, Void> removeTopCol = new TableColumn<>("取消置顶");
        removeTopCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("取消置顶");

            {
                btn.setOnAction(e -> {
                    WindowInfo info = getTableView().getItems().get(getIndex());
                    WindowUtils.removeTopMost(info.getHwnd());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        table.getColumns().addAll(titleCol, topMostCol, removeTopCol);
        table.setItems(windowList);

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
