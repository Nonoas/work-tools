package io.github.nonoas.worktools.platform.service

import io.github.nonoas.worktools.platform.pojo.params.FuncSettingQry
import cn.hutool.db.PageResult
import io.github.nonoas.worktools.platform.pojo.vo.FuncSettingVo

/**
 * @author Nonoas
 * @datetime 2022/7/19 22:52
 */
interface IFuncSettingService {
    fun search(qry: FuncSettingQry): PageResult<FuncSettingVo>?
}