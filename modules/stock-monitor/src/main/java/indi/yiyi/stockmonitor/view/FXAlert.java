package indi.yiyi.stockmonitor.view;

/**
 * @author Nonoas
 * @date 2025/8/21
 * @since
 */

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.scene.image.Image;

import java.util.Objects;
import java.util.Optional;

public final class FXAlert {
    private FXAlert() {
    }

    // 这里写死你的图标路径（推荐放 resources 目录）
    private static final String ICON_PATH = "/image/logo.png";
    private static final Image APP_ICON = new Image(Objects.requireNonNull(FXAlert.class.getResourceAsStream(ICON_PATH)));

    /**
     * 创建带固定图标的 Alert
     */
    private static Alert create(Alert.AlertType type, Stage owner, String title, String header, String content) {
        Alert alert = new Alert(type);
        if (owner != null) alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // 给弹窗设置图标
        Window win = alert.getDialogPane().getScene().getWindow();
        if (win instanceof Stage stage) {
            stage.getIcons().add(APP_ICON);
        } else {
            // 有些情况 dialog 未立即创建窗口，需要监听
            alert.getDialogPane().sceneProperty().addListener((obs, o, n) -> {
                if (n != null) {
                    Window w = n.getWindow();
                    if (w instanceof Stage s) {
                        if (s.getIcons().isEmpty()) {
                            s.getIcons().add(APP_ICON);
                        }
                    }
                }
            });
        }

        return alert;
    }

    // 常用方法
    public static void info(Stage owner, String title, String content) {
        create(Alert.AlertType.INFORMATION, owner, title, null, content).showAndWait();
    }

    public static void error(Stage owner, String title, String content) {
        create(Alert.AlertType.ERROR, owner, title, null, content).showAndWait();
    }

    public static Optional<ButtonType> confirm(Stage owner, String title, String content) {
        return create(Alert.AlertType.CONFIRMATION, owner, title, null, content).showAndWait();
    }
}
