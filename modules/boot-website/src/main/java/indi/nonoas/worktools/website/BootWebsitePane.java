package indi.nonoas.worktools.website;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

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

        root.getChildren().add(buildForm());
        root.getChildren().add(buildSearchBox());
        root.getChildren().add(buildTable());
        getChildren().add(root);
    }

    /** ------------------ 表单 ------------------ **/
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

    /** ------------------ 搜索框 ------------------ **/
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

    /** ------------------ 表格 ------------------ **/
    private TableView<WebsiteData> buildTable() {
        TableColumn<WebsiteData, String> commandCol = new TableColumn<>("命令");
        commandCol.setCellValueFactory(c -> c.getValue().command);

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

        table.setOnMouseClicked(e -> {
            WebsiteData sel = table.getSelectionModel().getSelectedItem();
            if (sel != null) fillForm(sel);
        });

        return table;
    }

    /** ------------------ CRUD 方法 ------------------ **/

    private void addItem() {
        if (commandField.getText().isEmpty() || urlField.getText().isEmpty()) {
            alert("命令和网址不能为空");
            return;
        }
        data.add(new WebsiteData(
                commandField.getText(),
                urlField.getText(),
                aliasField.getText()
        ));
        clearForm();
    }

    private void updateItem() {
        WebsiteData sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            alert("请选择要修改的条目");
            return;
        }

        sel.command.set(commandField.getText());
        sel.url.set(urlField.getText());
        sel.alias.set(aliasField.getText());

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
    }

    private void alert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.show();
    }

    /** ------------------ 数据类 ------------------ **/
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
