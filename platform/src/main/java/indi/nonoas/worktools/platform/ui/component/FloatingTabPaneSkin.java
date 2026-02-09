package indi.nonoas.worktools.platform.ui.component;

import javafx.animation.FadeTransition;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class FloatingTabPaneSkin extends SkinBase<TabPane> {

    // 承载内容的容器，跟随 TabPane 大小缩放
    private final StackPane contentArea = new StackPane();
    // 悬浮的标签栏
    private final HBox headerBar = new HBox(8);

    public FloatingTabPaneSkin(TabPane tabPane) {
        super(tabPane);

        buildUI();
        bindTabs();
        initHoverBehavior();

        // 核心：直接将子节点加入 Skin 内部容器
        // 先加内容区，后加悬浮条，确保悬浮条遮盖在内容之上
        getChildren().addAll(contentArea, headerBar);
    }

    private void buildUI() {
        // 设置内容区背景色（对应你之前的逻辑）

        // 悬浮条基础样式
        headerBar.setPadding(new Insets(8, 16, 8, 16));
        headerBar.setMaxHeight(HBox.USE_PREF_SIZE);
        headerBar.setMaxWidth(HBox.USE_PREF_SIZE);

        // 初始状态隐藏
        headerBar.setVisible(false);
        headerBar.setOpacity(0);

        // 关键：防止鼠标点击到悬浮条的透明空白处时拦截事件
        headerBar.setPickOnBounds(false);
    }

    private void bindTabs() {
        TabPane pane = getSkinnable();

        // 监听标签列表变化
        rebuildHeaders();
        pane.getTabs().addListener((ListChangeListener<Tab>) c -> rebuildHeaders());

        // 监听选中项变化
        pane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            showContent(newTab);
        });

        // 初始显示
        showContent(pane.getSelectionModel().getSelectedItem());
    }

    private void rebuildHeaders() {
        headerBar.getChildren().clear();
        TabPane pane = getSkinnable();

        for (Tab tab : pane.getTabs()) {
            Button btn = new Button(tab.getText());
            // 建议在 CSS 中定义此样式
            btn.getStyleClass().add("floating-tab-button");

            btn.setOnAction(e -> pane.getSelectionModel().select(tab));
            headerBar.getChildren().add(btn);
        }
    }

    private void showContent(Tab tab) {
        contentArea.getChildren().clear();
        if (tab != null && tab.getContent() != null) {
            // 让内容自动填充 StackPane
            contentArea.getChildren().add(tab.getContent());
        }
    }

    private void initHoverBehavior() {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), headerBar);
        fadeIn.setToValue(1);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), headerBar);
        fadeOut.setToValue(0);

        // 监听 Skinnable (TabPane) 的鼠标事件
        getSkinnable().setOnMouseEntered(e -> {
            headerBar.setVisible(true);
            fadeOut.stop();
            fadeIn.playFromStart();
        });

        getSkinnable().setOnMouseExited(e -> {
            fadeIn.stop();
            fadeOut.playFromStart();
            fadeOut.setOnFinished(ev -> {
                if (headerBar.getOpacity() == 0) {
                    headerBar.setVisible(false);
                }
            });
        });
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        // x, y 是考虑了 Padding 后的起始坐标
        // w, h 是 TabPane 的实际可用宽高

        // 1. 内容区铺满
        contentArea.resizeRelocate(x, y, w, h);

        // 2. 布局子节点（如果内容区有手动布局逻辑）
        for (Node child : contentArea.getChildren()) {
            child.resizeRelocate(0, 0, w, h);
        }

        // 3. 悬浮条居中于底部
        double hw = headerBar.prefWidth(-1);
        double hh = headerBar.prefHeight(-1);

        headerBar.resizeRelocate(
                x + (w - hw) / 2, // 水平居中
                y + h - hh - 20,  // 距离底部 20 像素
                hw,
                hh
        );
    }
}