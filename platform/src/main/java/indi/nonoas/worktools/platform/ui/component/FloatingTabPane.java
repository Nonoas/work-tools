package indi.nonoas.worktools.platform.ui.component;

import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class FloatingTabPane extends TabPane {

    public FloatingTabPane() {

    }

    public Tab addTab(String title, Node content) {
        Tab tab = new Tab(title);
        tab.setContent(content);
        getTabs().add(tab);
        return tab;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new FloatingTabPaneSkin(this);
    }

}