package indi.yiyi.stockmonitor.view;


import github.nonoas.jfx.flat.ui.concurrent.TaskHandler;
import indi.yiyi.stockmonitor.data.StockerSuggestion;
import indi.yiyi.stockmonitor.enums.StockerQuoteProvider;
import indi.yiyi.stockmonitor.utils.StockerSuggestHttpUtil;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 股票搜索框
 *
 * @author huangshengsheng
 * @date 2025/8/23 15:59
 */

public class StockSearchDialog extends Dialog<StockerSuggestion> {

    private final TextField textField = new TextField();
    private final ListView<StockerSuggestion> listView = new ListView<>();

    public StockSearchDialog() {
        setTitle("股票搜索");
        initModality(Modality.APPLICATION_MODAL);

        textField.setPromptText("输入股票代码或名称，双击确认选择");
        textField.textProperty().addListener((observableValue, s, t1) -> {
            if (StringUtils.isBlank(t1)) {
                return;
            }
            doSearch();
        });

        VBox.setVgrow(listView, Priority.ALWAYS);

        Button searchButton = new Button("搜索");
        searchButton.setOnAction(e -> doSearch());

        VBox vbox = new VBox(8, textField, listView);

        vbox.setFillWidth(true);
        vbox.setPadding(new Insets(10));
        getDialogPane().setContent(vbox);

        // ListView 显示格式
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(StockerSuggestion item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getCode() + " - " + item.getName() + " (" + item.getMarket().getDesc() + ")");
                }
            }
        });

        // 双击选择 -> 设置结果并关闭
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !listView.getSelectionModel().isEmpty()) {
                StockerSuggestion selected = listView.getSelectionModel().getSelectedItem();
                setResult(selected);
                close();
            }
        });
        listView.setPlaceholder(new Label("在上方输入股票代码或名称，双击确认选择"));

        // Dialog 按钮
        getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
        getDialogPane().setPrefSize(300, 400);

        // resultConverter 用于处理按钮结果
        setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.CANCEL) {
                return null;
            }
            return getResult(); // 双击时已通过 setResult 设置
        });

        // ✅ 自动把焦点放到输入框
        Platform.runLater(textField::requestFocus);
    }

    private void doSearch() {
        String key = textField.getText().trim();
        if (key.isEmpty()) return;

        StockerQuoteProvider provider = StockerQuoteProvider.SINA; // 默认用 SINA

        new TaskHandler<List<StockerSuggestion>>()
                .whenCall(() -> StockerSuggestHttpUtil.INSTANCE.suggest(key, provider))
                .andThen(results -> listView.getItems().setAll(results)).handle();
    }
}

