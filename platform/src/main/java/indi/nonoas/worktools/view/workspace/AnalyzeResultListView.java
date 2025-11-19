package indi.nonoas.worktools.view.workspace;

import indi.nonoas.worktools.pojo.vo.ModifyItemVo;
import indi.nonoas.worktools.pojo.vo.ModifyItemVo;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.List;
import java.util.Optional;

/**
 * Json分析结果
 *
 * @author Nonoas
 * @date 2021/11/20
 */
public class AnalyzeResultListView extends ListView<ModifyItemVo> {

    private final ObservableList<ModifyItemVo> items = getItems();

    //声明私有静态对象，用volatile修饰
    private static volatile AnalyzeResultListView instance;

    private AnalyzeResultListView() {
        initView();
    }


    private void initView() {
        setCellFactory(param -> new ItemCell());
    }

    /**
     * 设置listview显示的数据
     */
    public void setData(List<ModifyItemVo> data) {
        this.items.setAll(data);
    }

    public List<ModifyItemVo> getData() {
        return this.items;
    }

    //对外提供获取实例对象的方法
    public static synchronized AnalyzeResultListView getInstance() {
        return Optional.ofNullable(instance).orElse(instance = new AnalyzeResultListView());
    }

    static class ItemCell extends ListCell<ModifyItemVo> {
        @Override
        protected void updateItem(ModifyItemVo item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {

                String desc = Optional.ofNullable(item.getDesc())
                        .orElse("")
                        .replace("\n", " ");

                CheckBox cb = new CheckBox();

                Label lb = new Label() {{
                    setPrefWidth(500);
                    setMaxWidth(Double.MAX_VALUE);
                    setTextOverrun(OverrunStyle.ELLIPSIS);
                    setText(item.getModifyNum() + " - " + desc + "-" + item.getVersionNO());
                }};

                HBox.setHgrow(lb, Priority.ALWAYS);
                HBox hBox = new HBox(10, cb, lb);
                hBox.setAlignment(Pos.CENTER_LEFT);
                // 数据绑定
                item.selectedProperty().bind(cb.selectedProperty());
                setGraphic(hBox);
            } else {
                setGraphic(null);
            }
        }
    }
}
