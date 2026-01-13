package indi.yiyi.stockmonitor.data;

/**
 * @author Nonoas
 * @date 2025/8/27
 * @since
 */
public record Stock(String marketCode, String stockCode) {
    public String key() {
        return marketCode + "_" + stockCode;
    }
}
