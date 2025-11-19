package indi.nonoas.worktools.pojo.po

import indi.nonoas.worktools.pojo.vo.PageParamsVo

/**
 * @author Nonoas
 * @date 2022/5/16
 */
class PageParamsPo {

    var paramCode: String? = null
    var id: Long = 0
    var paramVal: String? = null
    var lastUseTimestamp: String? = null

    fun convertVo(): PageParamsVo {
        val po = this
        return PageParamsVo().apply {
            id = po.id
            paramCode = po.paramCode
            paramVal = po.paramVal
        }
    }
}