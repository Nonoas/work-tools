package indi.nonoas.worktools.service.impl

import cn.hutool.core.collection.CollUtil
import cn.hutool.db.PageResult
import indi.nonoas.worktools.dao.FuncSettingDao
import indi.nonoas.worktools.pojo.params.FuncSettingQry
import indi.nonoas.worktools.pojo.po.FuncSettingPo
import indi.nonoas.worktools.pojo.vo.FuncSettingVo
import indi.nonoas.worktools.service.IFuncSettingService
import java.util.function.Consumer

/**
 * @author Nonoas
 * @datetime 2022/7/19 22:53
 */
class FuncSettingService : IFuncSettingService {

    private val dao = FuncSettingDao()

    override fun search(qry: FuncSettingQry): PageResult<FuncSettingVo> {
        val pos = dao.pageBy(qry) ?: return PageResult<FuncSettingVo>()

        val vos = PageResult<FuncSettingVo>()
        vos.totalPage = pos.totalPage
        vos.total = pos.total
        pos.forEach(Consumer { po: FuncSettingPo -> vos.add(po.convertVo()) })
        return vos
    }
}