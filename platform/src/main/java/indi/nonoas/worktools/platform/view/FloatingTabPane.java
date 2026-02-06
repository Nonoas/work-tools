package indi.nonoas.worktools.platform.view;

import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class FloatingTabPane extends StackPane {
    private final TabPane tabPane = new TabPane();
    private final StackPane contentArea = new StackPane();
    private final double HEADER_HEIGHT = 40.0; // 标签栏高度
    private final double SENSITIVE_ZONE = 5.0;  // 感应区

    public FloatingTabPane() {
        // 1. 设置内容区域
        // 这里我们手动绑定：当 Tab 选择变化时，更新内容区的显示
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                contentArea.getChildren().setAll(newTab.getContent());
            }
        });

        // 2. 隐藏原生的 TabPane 内容区（CSS 方式最彻底）
        // 强制让 TabPane 的高度只等于标签栏的高度
        tabPane.setMaxHeight(HEADER_HEIGHT);
        tabPane.setMinHeight(HEADER_HEIGHT);
        tabPane.setTranslateY(HEADER_HEIGHT - SENSITIVE_ZONE);

        // 3. 布局逻辑
        // contentArea 放在底层，tabPane 浮在顶层并对齐底部
        this.getChildren().addAll(contentArea, tabPane);
        StackPane.setAlignment(tabPane, javafx.geometry.Pos.BOTTOM_CENTER);

        // 4. 动画逻辑
        TranslateTransition showAnim = new TranslateTransition(Duration.millis(200), tabPane);
        TranslateTransition hideAnim = new TranslateTransition(Duration.millis(250), tabPane);

        tabPane.setOnMouseEntered(e -> {
            hideAnim.stop();
            showAnim.setToY(0);
            showAnim.play();
        });

        tabPane.setOnMouseExited(e -> {
            showAnim.stop();
            hideAnim.setToY(HEADER_HEIGHT - SENSITIVE_ZONE);
            hideAnim.play();
        });

        // 5. 样式美化（关键：隐藏 TabPane 的内容背景）
        tabPane.setStyle("-fx-tab-max-height: " + (HEADER_HEIGHT - 5) + "px;");
        // 使用 Lookup 在初始化后隐藏内容区域
        tabPane.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                Node content = tabPane.lookup(".tab-content-area");
                if (content != null) content.setManaged(false); content.setVisible(false);
            }
        });
    }

    public void addTab(String title, Node content) {
        Tab tab = new Tab(title);
        tab.setContent(content);
        tabPane.getTabs().add(tab);

        // 默认显示第一个
        if (tabPane.getTabs().size() == 1) {
            contentArea.getChildren().setAll(content);
        }
    }
}