package indi.nonoas.worktools.platform.pojo.po

import indi.nonoas.worktools.platform.pojo.vo.FuncSettingVo

/**
 * @author Nonoas
 * @date 2022/1/26
 */
class FuncSettingPo {
    var funcCode: String? = null
    var funcName: String? = null
    var isEnableFlag = false

    fun convertVo(): FuncSettingVo {
        val po = this;
        return FuncSettingVo().apply {
            setFuncCode(po.funcCode)
            setFuncName(po.funcName)
            setEnableFlag(po.isEnableFlag)
        }
    }
}