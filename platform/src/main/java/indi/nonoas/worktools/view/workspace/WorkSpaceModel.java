package indi.nonoas.worktools.view.workspace;

import javafx.beans.property.SimpleStringProperty;

/**
 * 工作区间界面数据
 * @author Nonoas
 * @date 2022/5/11
 */
public class WorkSpaceModel {
    /**
     * 工作区间路径属性
     */
    private final SimpleStringProperty workSpacePath = new SimpleStringProperty();

    /**
     * json 代码属性
     */
    private final SimpleStringProperty jsonCode = new SimpleStringProperty();

    /**
     * 查询文本
     */
    private final SimpleStringProperty searchText = new SimpleStringProperty();


    public String getWorkSpacePath() {
        return workSpacePath.get();
    }

    public SimpleStringProperty workSpacePathProperty() {
        return workSpacePath;
    }

    public void setWorkSpacePath(String workSpacePath) {
        this.workSpacePath.set(workSpacePath);
    }

    public String getJsonCode() {
        return jsonCode.get();
    }

    public SimpleStringProperty jsonCodeProperty() {
        return jsonCode;
    }

    public void setJsonCode(String jsonCode) {
        this.jsonCode.set(jsonCode);
    }

    public String getSearchText() {
        return searchText.get();
    }

    public SimpleStringProperty searchTextProperty() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText.set(searchText);
    }
}