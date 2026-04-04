package io.github.nonoas.worktools.platform.service;

import cn.hutool.db.PageResult;
import io.github.nonoas.worktools.platform.pojo.params.ModifyItemQry;
import io.github.nonoas.worktools.platform.pojo.po.ModifyItemPo;
import io.github.nonoas.worktools.platform.pojo.vo.ModifyItemVo;

import java.util.List;
import java.util.Optional;

/**
 * @author Nonoas
 * @datetime 2022/5/11 20:42
 */
public interface IWorkSpaceService {

    /**
     * 查询工作区间
     * @param qry 工作区间数据
     */
    PageResult<ModifyItemPo> queryByKeywords(ModifyItemQry qry);

    /**
     * 通过工作区间目录查询
     * @param workspace 工作区间路径
     */
    List<ModifyItemVo> queryByWorkspace(String workspace);

    Optional<PageResult<ModifyItemPo>> pageBy(ModifyItemQry qry);
}
