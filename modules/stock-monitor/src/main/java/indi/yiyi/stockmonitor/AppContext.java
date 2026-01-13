package indi.yiyi.stockmonitor;

import javafx.stage.Stage;

/**
 * @author huangshengsheng
 * @date 2025/8/26 17:49
 */
public class AppContext {
    private static Stage mainStage = null;

    public static Stage getMainStage() {
        return mainStage;
    }

    public static void setMainStage(Stage stage) {
        AppContext.mainStage = stage;
    }
}