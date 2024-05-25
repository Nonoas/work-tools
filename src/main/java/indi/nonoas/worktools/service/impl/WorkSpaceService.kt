package indi.nonoas.worktools.service.impl;

import cn.hutool.db.Db
import cn.hutool.db.Entity
import cn.hutool.db.PageResult
import indi.nonoas.worktools.dao.ModifyItemsDao
import indi.nonoas.worktools.pojo.params.ModifyItemQry
import indi.nonoas.worktools.pojo.po.ModifyItemPo
import indi.nonoas.worktools.pojo.vo.ModifyItemVo
import indi.nonoas.worktools.service.IWorkSpaceService
import indi.nonoas.worktools.utils.BeanUtil
import indi.nonoas.worktools.utils.DBUtil
import java.util.*

/**
 * @author Nonoas
 * @datetime 2022/5/11 20:43
 */
class WorkSpaceService : IWorkSpaceService {

    private val dao = ModifyItemsDao()

    override fun queryByKeywords(qry: ModifyItemQry): PageResult<ModifyItemPo>? {
        return dao.pagedByKeyWords(qry)
    }

    override fun queryByWorkspace(workspace: String?): List<ModifyItemVo> {
        val entity = Entity.create("modify_items").set("workspace", workspace)
        val pos = DBUtil.use().findAll(entity, ModifyItemPo::class.java)
        pos.sortByDescending { it.modifyTime }
        return pos.map(ModifyItemPo::convertVo).toList()
    }

    override fun pageBy(qry: ModifyItemQry): Optional<PageResult<ModifyItemPo>> {
        val result = dao.pagedBy(qry) ?: return Optional.empty();
        val rs = PageResult<ModifyItemPo>().apply {
            total = result.total
            totalPage = result.totalPage
        }
        result.forEach {
            rs.add(BeanUtil.mapToBean(it, ModifyItemPo::class.java))
        }
        return Optional.of(rs)
    }

}
