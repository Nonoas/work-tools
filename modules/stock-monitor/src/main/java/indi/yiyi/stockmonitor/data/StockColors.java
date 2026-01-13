package indi.yiyi.stockmonitor.data;

/**
 * @author huangshengsheng
 * @date 2025/11/1 9:54
 */
import javafx.scene.paint.Color;

/**
 * 封装股票上涨、下跌和平盘的颜色设置。
 */
public class StockColors {
    private final Color upColor;
    private final Color downColor;
    private final Color flatColor;

    public StockColors(Color upColor, Color downColor, Color flatColor) {
        this.upColor = upColor;
        this.downColor = downColor;
        this.flatColor = flatColor;
    }

    // Getter 方法
    public Color getUpColor() {
        return upColor;
    }

    public Color getDownColor() {
        return downColor;
    }

    public Color getFlatColor() {
        return flatColor;
    }

    @Override
    public String toString() {
        return "StockColors{" +
                "upColor=" + upColor +
                ", downColor=" + downColor +
                ", flatColor=" + flatColor +
                '}';
    }
}