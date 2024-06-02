package indi.nonoas.worktools.pojo.vo

import cn.hutool.core.bean.BeanUtil
import indi.nonoas.worktools.pojo.po.RtpLinkListPo

/**
 * 最近
 * @author huangshengsheng
 * @date 2024/5/16 10:11
 */

class RtpLinkListVo {
    var id: String? = null
    var name: String? = null
    lateinit var link: String
    var lastUseTimestamp: Long = 0

    fun covertPo(): RtpLinkListPo {
        return BeanUtil.copyProperties(this, RtpLinkListPo::class.java)
    }
}