package indi.nonoas.worktools.platform.pojo.dto

import indi.nonoas.worktools.platform.pojo.vo.FuncSettingVo

/**
 * @author Nonoas
 * @date 2022/1/26
 */
class FuncSettingDto {
    lateinit var funcCode: String
    var funcName: String? = null
    var isEnableFlag: Boolean = false

    fun convertVo(): FuncSettingVo {
        val po = this;
        return FuncSettingVo().apply {
            setFuncCode(po.funcCode)
            setFuncName(po.funcName)
            setEnableFlag(po.isEnableFlag)
        }
    }
}
