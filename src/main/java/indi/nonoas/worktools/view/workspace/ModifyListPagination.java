package indi.nonoas.worktools.view.workspace;

import cn.hutool.core.util.StrUtil;
import cn.hutool.db.PageResult;
import indi.nonoas.worktools.pojo.params.ModifyItemQry;
import indi.nonoas.worktools.pojo.po.ModifyItemPo;
import indi.nonoas.worktools.pojo.vo.ModifyItemVo;
import indi.nonoas.worktools.service.impl.WorkSpaceService;
import indi.nonoas.worktools.ui.Reinitializable;
import indi.nonoas.worktools.ui.TaskHandler;
import indi.nonoas.worktools.pojo.params.ModifyItemQry;
import indi.nonoas.worktools.pojo.po.ModifyItemPo;
import indi.nonoas.worktools.pojo.vo.ModifyItemVo;
import indi.nonoas.worktools.service.IWorkSpaceService;
import indi.nonoas.worktools.service.impl.WorkSpaceService;
import indi.nonoas.worktools.ui.Reinitializable;
import indi.nonoas.worktools.ui.TaskHandler;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Pagination;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 工作区间分页查询组件
 *
 * @author Nonoas
 * @date 2022/6/16
 */
public class ModifyListPagination extends Pagination implements Reinitializable {

    private final int PAGE_SIZE = 15;

    ModifyItemQry qry = new ModifyItemQry();

    /**
     * 储存工作区间路径
     */
    private final SimpleStringProperty workspace = new SimpleStringProperty();

    private final ModifyListView lv = new ModifyListView();

    private final IWorkSpaceService service = new WorkSpaceService();

    public ModifyListPagination(String path) {
        workspace.set(path);
        workspace.addListener((observable, oldValue, newValue) -> onPageChanged(0));
        lv.setOnItemDelete(cell -> searchByKeyWords(qry));
        setPageFactory(this::onPageChanged);
        setCurrentPageIndex(0);
    }

    /**
     * 页面切换时调用
     *
     * @param pageNo 当前页码
     * @return 刷新后的listView
     */
    private ModifyListView onPageChanged(Integer pageNo) {
        qry.setWorkSpace(workspace.get());
        refreshData(pageNo);
        return lv;
    }

    /**
     * 关键字查询
     *
     * @param qry 查找
     */
    public void searchByKeyWords(ModifyItemQry qry) {
        this.qry = qry;
        refreshData(0);
        setCurrentPageIndex(0);
    }

    private void refreshData(Integer pageNo) {
        if (StrUtil.isBlank(qry.getWorkSpace())) {
            return;
        }
        qry.setPageNo(pageNo);
        qry.setPageSize(PAGE_SIZE);
        lv.setItems(null);

        new TaskHandler<PageResult<ModifyItemPo>>()
                .whenCall(() -> service.queryByKeywords(qry))
                .andThen(result -> {
                    setPageCount(Math.max(result.getTotalPage(), 1));
                    List<ModifyItemVo> vos = result.stream()
                            .map(ModifyItemPo::convertVo)
                            .collect(Collectors.toList());
                    lv.setItems(FXCollections.observableArrayList(vos));
                }).handle();

    }

    public String getWorkspace() {
        return workspace.get();
    }

    public SimpleStringProperty workspaceProperty() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace.set(workspace);
    }

    @Override
    public void reInit() {
        onPageChanged(0);
    }


}
