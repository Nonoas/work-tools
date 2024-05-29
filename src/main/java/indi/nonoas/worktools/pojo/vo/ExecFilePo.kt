package indi.nonoas.worktools.pojo.vo

import com.alibaba.fastjson2.JSON


class ExecFilePo {
    var id: Long = 0
    var name: String? = null
    var link: String? = null
    var createTimestamp: Long? = null
    var lastUseTimestamp: Long = System.currentTimeMillis()

    override fun toString(): String {
        return JSON.toJSONString(this)
    }
}
