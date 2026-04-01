package indi.nonoas.worktools.platform.ui.component;

import javafx.animation.FadeTransition;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class FloatingTabPaneSkin extends SkinBase<TabPane> {

    private static final double INSET = 16;
    private static final double HEADER_GAP = 8;

    private final StackPane contentArea = new StackPane();
    private final HBox headerBar = new HBox(8);
    private final Button toggleButton = new Button("插件");

    private boolean menuShowing = false;
    private boolean headerShown = false;
    private Scene boundScene;

    public FloatingTabPaneSkin(TabPane tabPane) {
        super(tabPane);

        buildUI();
        bindTabs();
        initToggleBehavior();
        installOutsideClickHide();

        getChildren().addAll(contentArea, headerBar, toggleButton);
    }

    private void buildUI() {
        headerBar.setPadding(new Insets(8, 12, 8, 12));
        headerBar.setMaxHeight(HBox.USE_PREF_SIZE);
        headerBar.setMaxWidth(HBox.USE_PREF_SIZE);
        headerBar.setVisible(false);
        headerBar.setOpacity(0);
        headerBar.setPickOnBounds(false);
        headerBar.getStyleClass().add("floating-tab-header");

        toggleButton.setFocusTraversable(false);
        toggleButton.getStyleClass().add("floating-tab-toggle");
    }

    private void bindTabs() {
        TabPane pane = getSkinnable();

        rebuildHeaders();
        pane.getTabs().addListener((ListChangeListener<Tab>) c -> FloatingTabPaneSkin.this.rebuildHeaders());

        pane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> showContent(newTab));

        showContent(pane.getSelectionModel().getSelectedItem());
    }

    private void rebuildHeaders() {
        headerBar.getChildren().clear();
        TabPane pane = getSkinnable();

        for (Tab tab : pane.getTabs()) {
            Button btn = new Button(tab.getText());
            btn.getStyleClass().add("floating-tab-button");

            ContextMenu contextMenu = tab.getContextMenu();
            if (contextMenu != null) {
                contextMenu.setOnShowing(e -> menuShowing = true);
                contextMenu.setOnHiding(e -> menuShowing = false);
                btn.setContextMenu(contextMenu);
                btn.setOnContextMenuRequested(e -> showHeader());
            }

            btn.setOnAction(e -> {
                pane.getSelectionModel().select(tab);
                hideHeader();
            });
            headerBar.getChildren().add(btn);
        }
    }

    private void showContent(Tab tab) {
        contentArea.getChildren().clear();
        if (tab != null && tab.getContent() != null) {
            contentArea.getChildren().add(tab.getContent());
        }
    }

    private void initToggleBehavior() {
        toggleButton.setOnAction(e -> {
            if (headerShown) {
                hideHeader();
            } else {
                showHeader();
            }
        });
    }

    private void installOutsideClickHide() {
        getSkinnable().sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (boundScene != null) {
                boundScene.removeEventFilter(MouseEvent.MOUSE_PRESSED, this::handleOutsideMousePressed);
            }
            boundScene = newScene;
            if (boundScene != null) {
                boundScene.addEventFilter(MouseEvent.MOUSE_PRESSED, this::handleOutsideMousePressed);
            }
        });

        Scene current = getSkinnable().getScene();
        if (current != null) {
            boundScene = current;
            boundScene.addEventFilter(MouseEvent.MOUSE_PRESSED, this::handleOutsideMousePressed);
        }
    }

    private void handleOutsideMousePressed(MouseEvent event) {
        if (!headerShown || menuShowing) {
            return;
        }

        Object target = event.getTarget();
        if (!(target instanceof Node targetNode)) {
            return;
        }

        if (isDescendantOf(targetNode, headerBar) || isDescendantOf(targetNode, toggleButton)) {
            return;
        }

        hideHeader();
    }

    private boolean isDescendantOf(Node node, Node ancestor) {
        Node cursor = node;
        while (cursor != null) {
            if (cursor == ancestor) {
                return true;
            }
            cursor = cursor.getParent();
        }
        return false;
    }

    private void showHeader() {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(90), headerBar);
        fadeIn.setToValue(1);

        headerBar.setVisible(true);
        fadeIn.playFromStart();
        headerShown = true;
    }

    private void hideHeader() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(80), headerBar);
        fadeOut.setToValue(0);
        fadeOut.playFromStart();
        fadeOut.setOnFinished(ev -> {
            if (!menuShowing && headerBar.getOpacity() == 0) {
                headerBar.setVisible(false);
            }
        });
        headerShown = false;
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        contentArea.resizeRelocate(x, y, w, h);

        for (Node child : contentArea.getChildren()) {
            child.resizeRelocate(0, 0, w, h);
        }

        double tw = toggleButton.prefWidth(-1);
        double th = toggleButton.prefHeight(-1);
        double toggleX = x + w - tw - INSET;
        double toggleY = y + h - th - INSET;
        toggleButton.resizeRelocate(toggleX, toggleY, tw, th);

        double hw = headerBar.prefWidth(-1);
        double hh = headerBar.prefHeight(-1);
        headerBar.resizeRelocate(
                x + w - hw - INSET,
                toggleY - hh - HEADER_GAP,
                hw,
                hh
        );
    }

    @Override
    public void dispose() {
        if (boundScene != null) {
            boundScene.removeEventFilter(MouseEvent.MOUSE_PRESSED, this::handleOutsideMousePressed);
            boundScene = null;
        }
        super.dispose();
    }
}
