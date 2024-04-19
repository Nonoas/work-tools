package indi.nonoas.worktools.service

import indi.nonoas.worktools.pojo.params.FuncSettingQry
import cn.hutool.db.PageResult
import indi.nonoas.worktools.pojo.vo.FuncSettingVo

/**
 * @author Nonoas
 * @datetime 2022/7/19 22:52
 */
interface IFuncSettingService {
    fun search(qry: FuncSettingQry): PageResult<FuncSettingVo>?
}