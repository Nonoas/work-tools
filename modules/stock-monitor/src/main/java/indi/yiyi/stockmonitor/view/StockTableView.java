package indi.yiyi.stockmonitor.view;

import github.nonoas.jfx.flat.ui.control.AlignedTableColumn;
import indi.yiyi.stockmonitor.AppContext;
import indi.yiyi.stockmonitor.data.StockGroup;
import indi.yiyi.stockmonitor.data.StockRow;
import indi.yiyi.stockmonitor.utils.GroupConfig;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * @author huangshengsheng
 * @date 2025/8/26 17:47
 */
public class StockTableView extends TableView<StockRow> {
    private final Map<String, StockRow> rowByKey = new ConcurrentHashMap<>();
    private final ObservableList<StockRow> data = FXCollections.observableArrayList();

    private static final PseudoClass UP = PseudoClass.getPseudoClass("up");
    private static final PseudoClass DOWN = PseudoClass.getPseudoClass("down");

    private StockGroup stockGroup;

    public StockTableView(StockGroup stockGroup) {
        this.stockGroup = stockGroup;

        TableColumn<StockRow, Number> colIndex = new AlignedTableColumn<>("序号", AlignedTableColumn.Alignment.CENTER);
        colIndex.setPrefWidth(40);
        colIndex.setMinWidth(50);
        colIndex.setCellValueFactory(c -> c.getValue().indexProperty());

        TableColumn<StockRow, String> colCode = new TableColumn<>("股票代码");
        colCode.setPrefWidth(120);
        colCode.setCellValueFactory(c -> c.getValue().codeProperty());

        TableColumn<StockRow, String> colName = new AlignedTableColumn<>("股票名称", AlignedTableColumn.Alignment.CENTER);
        colName.setPrefWidth(140);
        colName.setCellValueFactory(c -> c.getValue().nameProperty());

        TableColumn<StockRow, Number> colChangeRate = new AlignedTableColumn<>("涨跌幅", AlignedTableColumn.Alignment.CENTER);

        colChangeRate.setPrefWidth(100);
        colChangeRate.setCellValueFactory(c -> c.getValue().changeRateProperty());
        colChangeRate.setComparator(Comparator.comparingDouble(n -> n == null ? 0.0 : n.doubleValue()));
        // 显示成百分比文本
        colChangeRate.setCellFactory(percentCell());

        TableColumn<StockRow, Number> colPrice = new AlignedTableColumn<>("当前股价", AlignedTableColumn.Alignment.CENTER);
        colPrice.setPrefWidth(120);
        colPrice.setCellValueFactory(c -> c.getValue().priceProperty());
        colPrice.setCellFactory(formatNumber());

        TableColumn<StockRow, Number> colChangeAmt = new AlignedTableColumn<>("当日涨跌", AlignedTableColumn.Alignment.CENTER);
        colChangeAmt.setPrefWidth(120);
        colChangeAmt.setCellValueFactory(c -> c.getValue().changeAmtProperty());
        colChangeAmt.setCellFactory(formatNumber());

        getColumns().addAll(colIndex, colCode, colName, colChangeRate, colPrice, colChangeAmt);
        setItems(data);
        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // 行颜色：涨红、跌绿、平默认
        setRowFactory(tv -> {
            TableRow<StockRow> row = new TableRow<>();

            ChangeListener<Number> amtListener = (obs, ov, nv) -> applyPseudo(row);

            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (oldItem != null) {
                    oldItem.changeAmtProperty().removeListener(amtListener);
                }
                if (newItem != null) {
                    newItem.changeAmtProperty().addListener(amtListener);
                }
                applyPseudo(row);
            });

            row.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> applyPseudo(row));

            MenuItem del = new MenuItem("删除");
            MenuItem addToGroup = new MenuItem("修改分组");
            ContextMenu cm = new ContextMenu(addToGroup, del);

            // 仅在行非空时显示
            row.contextMenuProperty().bind(
                    javafx.beans.binding.Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(cm)
            );

            del.setOnAction(evt -> {
                StockRow item = row.getItem();
                if (item == null) return;

                var r = FXAlert.confirm(AppContext.getMainStage(), "确认删除", "确定要删除 " + item.getCode() + "（" + item.getName() + "）吗？");
                if (r.isEmpty() || r.get() != ButtonType.OK) return;

                boolean removeStock = GroupConfig.removeStock(stockGroup.getName(), item.getMarketCode(), item.getRawCode());
                if (!removeStock) {
                    new Alert(Alert.AlertType.ERROR, "从 CSV 移除失败，可能该条目不存在。").showAndWait();
                    return;
                }

                // 2) 从表格移除
                String key = item.getMarketCode() + "_" + item.getRawCode();
                rowByKey.remove(key);
                getItems().remove(item);

                // 3) 重新编号（可选）
                for (int i = 0; i < getItems().size(); i++) {
                    getItems().get(i).setIndex(i + 1);
                }
            });

            addToGroup.setOnAction(e -> {
                StockRow stock = row.getItem();
                if (stock != null) {
                    showModifyGroupDialog(stock);
                }
            });
            return row;
        });
    }

    private void showModifyGroupDialog(StockRow stock) {
        // 取出所有分组
        List<GroupConfig.Group> allGroups = GroupConfig.getGroups();

        // 当前在哪些分组里
        Set<String> currentGroups = allGroups.stream()
                .filter(g -> g.getStocks().stream()
                        .anyMatch(s -> s.marketCode().equals(stock.getMarketCode())
                                && s.stockCode().equals(stock.getRawCode())))
                .map(GroupConfig.Group::getName)
                .collect(Collectors.toSet());

        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setTitle("修改分组");
        dialog.setHeaderText("请选择股票【" + stock.getCode() + " - " + stock.getName() + "】所在的分组");
        dialog.initOwner(AppContext.getMainStage());

        ButtonType okButtonType = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        VBox box = new VBox(10);
        box.setPadding(new Insets(10));

        Map<String, CheckBox> checkBoxMap = new LinkedHashMap<>();
        for (GroupConfig.Group g : allGroups) {
            CheckBox cb = new CheckBox(g.getName());
            cb.setSelected(currentGroups.contains(g.getName()));
            checkBoxMap.put(g.getName(), cb);
            box.getChildren().add(cb);
        }

        dialog.getDialogPane().setContent(box);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return checkBoxMap.entrySet().stream()
                        .filter(e -> e.getValue().isSelected())
                        .map(Map.Entry::getKey)
                        .toList();
            }
            return null;
        });

        Optional<List<String>> result = dialog.showAndWait();
        result.ifPresent(selectedGroups -> {
            // 先从所有分组移除该股票
            for (GroupConfig.Group g : allGroups) {
                GroupConfig.removeStock(g.getName(), stock.getMarketCode(), stock.getRawCode());
            }

            // 再加入勾选的分组
            for (String gName : selectedGroups) {
                GroupConfig.addStock(gName, stock.getMarketCode(), stock.getRawCode());
            }

            // 刷新持久化文件
            GroupConfig.save();
        });
    }


    // 定义一个百分比单元格工厂
    private static Callback<TableColumn<StockRow, Number>, TableCell<StockRow, Number>> percentCell() {
        return col -> new TableCell<>() {
            @Override
            protected void updateItem(Number v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setText(null);
                } else {
                    setText(String.format(Locale.CHINA, "%.2f%%", v.doubleValue() * 100));
                    setStyle("-fx-alignment: " + AlignedTableColumn.Alignment.RIGHT.getCellCss() + ";");
                }
            }
        };
    }

    private Callback<TableColumn<StockRow, Number>, TableCell<StockRow, Number>> formatNumber() {
        return col -> new TableCell<>() {
            @Override
            protected void updateItem(Number value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(String.format(Locale.CHINA, "%.3f", value.doubleValue()));
                    setStyle("-fx-alignment: " + AlignedTableColumn.Alignment.RIGHT.getCellCss() + ";");
                }
            }
        };
    }

    private void applyPseudo(TableRow<StockRow> row) {
        StockRow item = row.getItem();
        boolean up = false, down = false;
        if (item != null && !row.isEmpty()) {
            double v = item.getChangeAmt();
            up = v > 0;
            down = v < 0;
        }
        row.pseudoClassStateChanged(UP, up);
        row.pseudoClassStateChanged(DOWN, down);
    }

    public boolean containsStockKey(String key) {
        return rowByKey.containsKey(key);
    }

    public Map<String, StockRow> getRowByKey() {
        return rowByKey;
    }

}