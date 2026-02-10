package indi.nonoas.worktools.platform.view;

import indi.nonoas.worktools.platform.ext.FuncPane;
import indi.nonoas.worktools.platform.ext.FuncPaneFactory;
import indi.nonoas.worktools.platform.global.Disposer;
import indi.nonoas.worktools.platform.ui.component.FloatingTabPane;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;

/**
 * @author huangshengsheng
 * @date 2026/2/10 16:16
 */
public class MainFuncPane extends FloatingTabPane {

    public Tab open(FuncPaneFactory factory) {
        FuncPane funcPane = factory.create();
        Disposer.register(funcPane);

        Parent rootView = funcPane.getRootView();
        Tab tab = new Tab(factory.getName(), rootView);

        ContextMenu contextMenu = new ContextMenu();
        MenuItem close = new MenuItem("关闭");
        close.setOnAction(actionEvent -> {
            Disposer.dispose(funcPane);
            getTabs().remove(tab);
        });

        contextMenu.getItems().add(close);

        tab.setContextMenu(contextMenu);

        getTabs().add(tab);
        return tab;
    }
}