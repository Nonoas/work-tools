package indi.nonoas.worktools.website;

import github.nonoas.jfx.flat.ui.AppState;
import indi.nonoas.worktools.platform.ui.component.FXAlert;
import indi.nonoas.worktools.website.dao.WebsiteDataDao;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.List;

public class BootWebsitePane extends StackPane {

    private final ObservableList<WebsiteData> data = FXCollections.observableArrayList();

    private final TextField commandField = new TextField();
    private final TextField urlField = new TextField();
    private final TextField aliasField = new TextField();
    private final TextField searchField = new TextField();

    private final TableView<WebsiteData> table = new TableView<>();

    public BootWebsitePane() {
        setPadding(new Insets(15));
        VBox root = new VBox(12);

        // 2. 加载现有数据
        loadDataFromDb();

        root.getChildren().add(buildForm());
        root.getChildren().add(buildSearchBox());
        root.getChildren().add(buildTable());
        getChildren().add(root);
    }

    /**
     * 从数据库读取并填充到表格
     */
    private void loadDataFromDb() {
        List<WebsiteData> all = WebsiteDataDao.INSTANCE.findAll();
        data.setAll(all);
    }

    /**
     * ------------------ 表单 ------------------
     **/
    private Pane buildForm() {
        VBox box = new VBox(10);

        Label title = new Label("添加 / 修改条目");
        title.setFont(Font.font(16));

        commandField.setPromptText("命令");
        urlField.setPromptText("网址");
        aliasField.setPromptText("网址简称");

        HBox row1 = new HBox(10, commandField, urlField, aliasField);

        Button addBtn = new Button("新增");
        Button updateBtn = new Button("修改");
        Button clearBtn = new Button("清空");

        addBtn.setOnAction(e -> addItem());
        updateBtn.setOnAction(e -> updateItem());
        clearBtn.setOnAction(e -> clearForm());

        HBox row2 = new HBox(10, addBtn, updateBtn, clearBtn);

        box.getChildren().addAll(title, row1, row2);
        return box;
    }

    /**
     * ------------------ 搜索框 ------------------
     **/
    private Pane buildSearchBox() {
        HBox box = new HBox(10);
        searchField.setPromptText("输入关键词搜索…");
        Button searchBtn = new Button("搜索");
        Button resetBtn = new Button("重置");

        searchBtn.setOnAction(e -> search());
        resetBtn.setOnAction(e -> resetSearch());

        box.getChildren().addAll(searchField, searchBtn, resetBtn);
        return box;
    }

    /**
     * ------------------ 表格 ------------------
     **/
    private TableView<WebsiteData> buildTable() {
        TableColumn<WebsiteData, String> commandCol = new TableColumn<>("命令");
        commandCol.setCellValueFactory(c -> c.getValue().command); // 使用Property

        TableColumn<WebsiteData, String> urlCol = new TableColumn<>("网址");
        urlCol.setCellValueFactory(c -> c.getValue().url);

        TableColumn<WebsiteData, String> aliasCol = new TableColumn<>("简称");
        aliasCol.setCellValueFactory(c -> c.getValue().alias);

        TableColumn<WebsiteData, Void> actionCol = new TableColumn<>("操作");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button del = new Button("删除");

            {
                del.setOnAction(e -> {
                    WebsiteData item = getTableView().getItems().get(getIndex());
                    // 数据库同步删除
                    WebsiteDataDao.INSTANCE.deleteByCommand(item.command.get());
                    data.remove(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(del);
                }
            }
        });

        table.getColumns().addAll(commandCol, urlCol, aliasCol, actionCol);
        table.setItems(data);
        table.setPrefHeight(300);

        // 设置之前提到的列自动调整策略
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.setOnMouseClicked(e -> {
            WebsiteData sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) fillForm(sel);
        });

        return table;
    }

    /**
     * ------------------ CRUD 方法 (已整合Dao) ------------------
     **/

    private void addItem() {
        String cmd = commandField.getText();
        String url = urlField.getText();
        String alias = aliasField.getText();

        if (cmd.isEmpty() || url.isEmpty()) {
            FXAlert.error(AppState.getStage(), "警告", "命令和网址不能为空");
            return;
        }

        WebsiteData newItem = new WebsiteData(cmd, url, alias);

        // 1. 存入数据库
        WebsiteDataDao.INSTANCE.saveOrUpdate(newItem);
        // 2. 更新UI列表
        data.add(newItem);

        clearForm();
    }

    private void updateItem() {
        WebsiteData sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            FXAlert.info(AppState.getStage(), "提示", "请选择要修改的条目");
            return;
        }

        // 更新属性
        sel.command.set(commandField.getText());
        sel.url.set(urlField.getText());
        sel.alias.set(aliasField.getText());

        // 1. 同步到数据库 (MERGE语句会根据PK自动更新)
        WebsiteDataDao.INSTANCE.saveOrUpdate(sel);

        // 2. 刷新UI
        table.refresh();
        clearForm();
    }

    private void search() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) return;

        table.setItems(data.filtered(b ->
                b.command.get().contains(keyword)
                        || b.url.get().contains(keyword)
                        || b.alias.get().contains(keyword)
        ));
    }

    private void resetSearch() {
        searchField.clear();
        table.setItems(data);
    }

    private void fillForm(WebsiteData b) {
        commandField.setText(b.command.get());
        urlField.setText(b.url.get());
        aliasField.setText(b.alias.get());
    }

    private void clearForm() {
        commandField.clear();
        urlField.clear();
        aliasField.clear();
        table.getSelectionModel().clearSelection();
    }

    /**
     * ------------------ 数据类 ------------------
     **/
    public static class WebsiteData {
        public final SimpleStringProperty command;
        public final SimpleStringProperty url;
        public final SimpleStringProperty alias;

        public WebsiteData(String c, String u, String a) {
            this.command = new SimpleStringProperty(c);
            this.url = new SimpleStringProperty(u);
            this.alias = new SimpleStringProperty(a);
        }
    }
}
