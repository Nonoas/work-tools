package io.github.nonoas.worktools.platform.service.impl

import cn.hutool.db.PageResult
import io.github.nonoas.worktools.platform.dao.FuncSettingDao
import io.github.nonoas.worktools.platform.pojo.params.FuncSettingQry
import io.github.nonoas.worktools.platform.pojo.po.FuncSettingPo
import io.github.nonoas.worktools.platform.pojo.vo.FuncSettingVo
import io.github.nonoas.worktools.platform.service.IFuncSettingService
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