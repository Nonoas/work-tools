package io.github.nonoas.worktools.platform.service.impl

import cn.hutool.db.Entity
import cn.hutool.db.PageResult
import io.github.nonoas.worktools.platform.dao.ModifyItemsDao
import io.github.nonoas.worktools.platform.pojo.params.ModifyItemQry
import io.github.nonoas.worktools.platform.pojo.po.ModifyItemPo
import io.github.nonoas.worktools.platform.pojo.vo.ModifyItemVo
import io.github.nonoas.worktools.platform.service.IWorkSpaceService
import io.github.nonoas.worktools.platform.utils.BeanUtil
import io.github.nonoas.worktools.platform.utils.DBUtil
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
