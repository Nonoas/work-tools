package indi.yiyi.stockmonitor.data;

import indi.yiyi.stockmonitor.view.StockTableView;

/**
 * @author Nonoas
 * @date 2025/8/27
 * @since
 */
public class StockGroup {
    private final String name;
    private final StockTableView tableView;

    public StockGroup(String name) {
        this.name = name;
        this.tableView = new StockTableView(this);
    }

    public String getName() { return name; }
    public StockTableView getTableView() { return tableView; }
}
