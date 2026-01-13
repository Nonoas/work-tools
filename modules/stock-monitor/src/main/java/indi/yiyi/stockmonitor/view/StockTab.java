package indi.yiyi.stockmonitor.view;

import indi.yiyi.stockmonitor.data.StockGroup;
import javafx.geometry.Insets;
import javafx.scene.control.Tab;
import javafx.scene.layout.StackPane;

/**
 * @author Nonoas
 * @date 2025/8/27
 * @since 1.0.0
 */
public class StockTab extends Tab {

    private final StockGroup stockGroup;

    public StockTab(StockGroup group) {
        super(group.getName());
        this.stockGroup = group;
        StackPane stackPane = new StackPane(group.getTableView());
        stackPane.setPadding(new Insets(10));
        setContent(stackPane);
    }

    public StockGroup getStockGroup() {
        return stockGroup;
    }

    public StockTableView getTableView() {
        return stockGroup.getTableView();
    }
}
