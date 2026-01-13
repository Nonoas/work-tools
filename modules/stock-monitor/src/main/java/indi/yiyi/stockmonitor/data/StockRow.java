package indi.yiyi.stockmonitor.data;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * @author Nonoas
 * @date 2025/8/20
 * @since
 */
// ========= 数据模型 =========
public class StockRow {
    private final String marketCode; // 原始字段，为了 key 组合
    private final String rawCode;    // 原始股票代码（不含市场前缀）
    private final IntegerProperty index = new SimpleIntegerProperty(0);
    private final StringProperty code = new SimpleStringProperty("");
    private final StringProperty name = new SimpleStringProperty("");
    private final DoubleProperty price = new SimpleDoubleProperty(0);
    private final DoubleProperty changeRate = new SimpleDoubleProperty(0);
    private final StringProperty changeRateStr = new SimpleStringProperty("");
    private final DoubleProperty changeAmt = new SimpleDoubleProperty(0);

    public StockRow(int index,
                    String marketCode,
                    String rawCode,
                    String codeShown,
                    String name,
                    double price,
                    double changeRate,
                    String changeRateStr,
                    double changeAmt) {
        this.marketCode = marketCode;
        this.rawCode = rawCode;
        setIndex(index);
        setCode(codeShown);
        setName(name);
        setPrice(price);
        setChangeRate(changeRate);
        setChangeRateStr(changeRateStr);
        setChangeAmt(changeAmt);
    }

    // getters for key
    public String getMarketCode() {
        return marketCode;
    }

    public String getRawCode() {
        return rawCode;
    }

    // properties
    public IntegerProperty indexProperty() {
        return index;
    }

    public StringProperty codeProperty() {
        return code;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public DoubleProperty priceProperty() {
        return price;
    }

    public DoubleProperty changeRateProperty() {
        return changeRate;
    }

    public StringProperty changeRateStrProperty() {
        return changeRateStr;
    }

    public DoubleProperty changeAmtProperty() {
        return changeAmt;
    }

    // getters/setters (for convenience)
    public int getIndex() {
        return index.get();
    }

    public void setIndex(int v) {
        index.set(v);
    }

    public String getCode() {
        return code.get();
    }

    public void setCode(String v) {
        code.set(v);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String v) {
        name.set(v);
    }

    public double getPrice() {
        return price.get();
    }

    public void setPrice(double v) {
        price.set(v);
    }

    public double getChangeRate() {
        return changeRate.get();
    }

    public void setChangeRate(double v) {
        changeRate.set(v);
    }

    public String getChangeRateStr() {
        return changeRateStr.get();
    }

    public void setChangeRateStr(String v) {
        changeRateStr.set(v);
    }

    public double getChangeAmt() {
        return changeAmt.get();
    }

    public void setChangeAmt(double v) {
        changeAmt.set(v);
    }
}