package indi.nonoas.worktools.windowsutil;


import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import github.nonoas.jfx.flat.ui.concurrent.TaskHandler;
import github.nonoas.jfx.flat.ui.control.Switch;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

    private TableView<WindowInfo> table = new TableView<>();

    private WindowTablePane() {
        setSpacing(5);
        setPadding(new Insets(5));

        // 标题列
        TableColumn<WindowInfo, String> titleCol = new TableColumn<>("窗口标题");
        titleCol.setCellValueFactory(data -> data.getValue().titleProperty());
        titleCol.setCellFactory(new Callback<TableColumn<WindowInfo, String>, TableCell<WindowInfo, String>>() {
            @Override
            public TableCell<WindowInfo, String> call(TableColumn<WindowInfo, String> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                        } else {
                            // 1. 尝试安全获取数据，避免异常副作用
                            try {
                                // **注意：getTableColumn() 确保我们获取的是当前列的数据模型**
                                // getIndex() 是获取当前单元格的行索引
                                WindowInfo info = getTableView().getItems().get(getIndex());

                                // 确保 getIcon() 快速且不触发阻塞操作
                                ImageView icon = info.getIcon();
                                String title = info.getTitle();

                                // 2. 正常构建 UI
                                HBox hBox = new HBox(10.0, icon, new Label(title));
                                hBox.setAlignment(Pos.CENTER_LEFT);
                                setGraphic(hBox);

                            } catch (IndexOutOfBoundsException e) {
                                // 预防 getIndex() 或 getItems().get() 出现问题
                                setGraphic(null);
                            } catch (Exception e) {
                                // 3. **关键点：如果这里出现异常，绝不能直接调用 showAndWait**
                                // 应该记录日志，或者显示一个占位符，然后稍后安全地处理异常
                                System.err.println("Error rendering TableCell: " + e.getMessage());
                                setGraphic(new Label("渲染错误"));
                            }
                        }

                    }
                };
            }
        });
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
        List<WindowInfo> tmpList = FXCollections.observableArrayList();
        new TaskHandler<List<WindowInfo>>().whenCall(() -> {
                    List<DesktopWindow> desktopWindows = com.sun.jna.platform.WindowUtils.getAllWindows(true);
                    for (DesktopWindow dw : desktopWindows) {
                        String title = dw.getTitle();
                        if (!title.isEmpty()) {
                            HWND hwnd = dw.getHWND();
                            BufferedImage windowIcon = com.sun.jna.platform.WindowUtils.getWindowIcon(hwnd);
                            ImageView imageView;
                            if (windowIcon != null) {
                                WritableImage fxImage = SwingFXUtils.toFXImage(windowIcon, null);
                                imageView = new ImageView(fxImage);
                            } else {
                                imageView = new ImageView();
                            }
                            imageView.setFitWidth(16);
                            imageView.setFitHeight(16);
                            tmpList.add(new WindowInfo(dw.getHWND(), title, imageView));
                        }
                    }
                    return tmpList;
                })
                .andThen(windowList::addAll)
                .handle();
    }

    public void queryFilter(String keyword) {
        if (null == keyword || keyword.isBlank()) {
            table.setItems(windowList);
            return;
        }
        ObservableList<WindowInfo> collect = windowList.stream()
                .filter(windowInfo -> windowInfo.getTitle().contains(keyword))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        table.setItems(collect);
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
