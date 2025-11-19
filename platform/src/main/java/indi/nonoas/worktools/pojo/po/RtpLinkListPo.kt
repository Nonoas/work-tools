package indi.nonoas.worktools.pojo.po

import cn.hutool.core.bean.BeanUtil
import indi.nonoas.worktools.pojo.vo.RtpLinkListVo

/**
 * 最近
 * @author huangshengsheng
 * @date 2024/5/16 10:11
 */

class RtpLinkListPo {
    var id: String? = null
    var name: String? = null
    var link: String? = null
    var lastUseTimestamp: Long = 0

    fun covertVo(): RtpLinkListVo {
        return BeanUtil.copyProperties(this, RtpLinkListVo::class.java)
    }
}