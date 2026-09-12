package io.github.nonoas.worktools.platform.view;

import io.github.nonoas.worktools.platform.ext.FuncPane;
import io.github.nonoas.worktools.platform.ext.FuncPaneFactory;
import io.github.nonoas.worktools.platform.global.Disposer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 当前打开的功能页容器。
 */
public class MainFuncPane extends StackPane {

    private final ObservableList<OpenFuncPage> openPages = FXCollections.observableArrayList();
    private final ObservableList<OpenFuncPage> readOnlyOpenPages = FXCollections.unmodifiableObservableList(openPages);
    private final Map<String, OpenFuncPage> pageMap = new LinkedHashMap<>();
    private final StringProperty activeFuncCode = new SimpleStringProperty();
    private Runnable onEmpty;

    public OpenFuncPage openOrSelect(FuncPaneFactory factory) {
        String funcCode = factory.getCode();
        OpenFuncPage existed = pageMap.get(funcCode);
        if (existed != null) {
            selectPage(funcCode);
            return existed;
        }

        FuncPane funcPane = factory.create();
        Disposer.register(funcPane);

        OpenFuncPage page = new OpenFuncPage(funcCode, factory.getName(), funcPane, funcPane.getRootView());
        pageMap.put(funcCode, page);
        openPages.add(page);
        selectPage(funcCode);
        return page;
    }

    public boolean selectPage(String funcCode) {
        OpenFuncPage page = pageMap.get(funcCode);
        if (page == null) {
            return false;
        }

        getChildren().setAll(page.getRootView());
        activeFuncCode.set(funcCode);
        for (OpenFuncPage openPage : openPages) {
            openPage.setActive(openPage == page);
        }
        return true;
    }

    public boolean closePage(String funcCode) {
        OpenFuncPage page = pageMap.get(funcCode);
        if (page == null) {
            return false;
        }

        int index = openPages.indexOf(page);
        boolean active = page.isActive();
        pageMap.remove(funcCode);
        openPages.remove(page);
        Disposer.dispose(page.getFuncPane());

        if (openPages.isEmpty()) {
            getChildren().clear();
            activeFuncCode.set(null);
            if (onEmpty != null) {
                onEmpty.run();
            }
            return true;
        }

        if (active) {
            OpenFuncPage nextPage = openPages.get(Math.min(index, openPages.size() - 1));
            selectPage(nextPage.getFuncCode());
        }
        return true;
    }

    public ObservableList<OpenFuncPage> getOpenPages() {
        return readOnlyOpenPages;
    }

    public ReadOnlyStringProperty activeFuncCodeProperty() {
        return activeFuncCode;
    }

    public String getActiveFuncCode() {
        return activeFuncCode.get();
    }

    public void setOnEmpty(Runnable onEmpty) {
        this.onEmpty = onEmpty;
    }

    public static class OpenFuncPage {
        private final String funcCode;
        private final String funcName;
        private final FuncPane funcPane;
        private final Parent rootView;
        private final BooleanProperty active = new SimpleBooleanProperty(false);

        private OpenFuncPage(String funcCode, String funcName, FuncPane funcPane, Parent rootView) {
            this.funcCode = funcCode;
            this.funcName = funcName;
            this.funcPane = funcPane;
            this.rootView = rootView;
        }

        public String getFuncCode() {
            return funcCode;
        }

        public String getFuncName() {
            return funcName;
        }

        public FuncPane getFuncPane() {
            return funcPane;
        }

        public Parent getRootView() {
            return rootView;
        }

        public boolean isActive() {
            return active.get();
        }

        public BooleanProperty activeProperty() {
            return active;
        }

        private void setActive(boolean active) {
            this.active.set(active);
        }
    }
}
