package indi.nonoas.worktools.pojo.po

import cn.hutool.db.Entity
import indi.nonoas.worktools.pojo.vo.ModifyItemVo

/**
 * @author Nonoas
 * @date 2022/5/9
 */
class ModifyItemPo {

    /**
     * 绝对路径
     */
    var absolutePath: String? = null

    /**
     * 修改单号
     */
    var modifyNum: String? = null

    /**
     * 需求编号
     */
    var reqNums: String? = null

    /**
     * 修改单时间戳
     */
    var timestamp: Long = 0

    /**
     * 描述
     */
    var desc: String? = ""

    /**
     * 创建时间
     */
    var createTime: Long? = 0

    /**
     * 更新时间
     */
    var modifyTime: Long? = 0

    /**
     * 修改原因
     */
    var modifyReason: String? = null


    /**
     * 修改版本号
     */
    var versionNo: String? = null

    /**
     * 工作区间
     */
    var workspace: String? = null

    /**
     * 是否忽略空值
     */
    fun convertEntry(ignoreNullValue: Boolean): Entity {
        return Entity.create("modify_items").parseBean(this, true, ignoreNullValue)
    }

    override fun toString(): String {
        return "$modifyNum-$desc-$versionNo"
    }

    fun convertVo(): ModifyItemVo {
        val po = this;
        return ModifyItemVo().apply {
            absolutePath = po.absolutePath
            modifyNum = po.modifyNum
            timestamp = po.timestamp
            desc = po.desc
            versionNO = po.versionNo
            reqNums = po.reqNums
        }
    }

}
