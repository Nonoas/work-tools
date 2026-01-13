package indi.yiyi.stockmonitor.utils;

import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * @author Nonoas
 * @date 2025/8/21
 * @since
 */
public class UIUtil {
    public static void setDialogIcon(Alert alert, Stage owner) {
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        if (owner != null && !owner.getIcons().isEmpty()) {
            stage.getIcons().add(owner.getIcons().get(0));
        }
    }

    public static String toWebColor(Color color) {
        // 格式化为 #RRGGBB，不包含透明度 (因为 derive() 不喜欢 #RRGGBBAA)
        return String.format("#%02x%02x%02x",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255)
        );
    }
}
