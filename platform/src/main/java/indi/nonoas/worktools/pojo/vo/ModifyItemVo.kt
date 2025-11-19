package indi.nonoas.worktools.pojo.vo

import indi.nonoas.worktools.pojo.po.ModifyItemPo
import indi.nonoas.worktools.utils.RegexUtil
import javafx.beans.property.SimpleBooleanProperty
import java.lang.Integer.min

/**
 * @author Nonoas
 * @date 2022/5/9
 */
class ModifyItemVo {

    /**
     * 绝对路径
     */
    var absolutePath: String? = null

    private val selected = SimpleBooleanProperty()

    /**
     * 修改单号
     */
    var modifyNum: String? = null
    var timestamp: Long = 0

    /**
     * 修改原因
     */
    var modifyReason: String? = null
        private set

    /**
     * 描述
     */
    var desc: String? = ""

    /**
     * 修改版本号
     */
    var versionNO: String? = null

    /**
     * 需求编号
     */
    var reqNums: String? = null

    fun setModifyReason(modifyReason: String) {
        this.modifyReason = modifyReason
            .replace("<br>", "\n")
            .replace("<[^<>]*>".toRegex(), "")
            .replace("&[^&;]*;".toRegex(), "")
    }

    override fun toString(): String {
        return "$modifyNum-${desc?.trim()}-$versionNO"
    }

    fun getSelected(): Boolean {
        return selected.get()
    }

    fun selectedProperty(): SimpleBooleanProperty {
        return selected
    }

    fun setSelected(selected: Boolean) {
        this.selected.set(selected)
    }

    /**
     * 截取版本号后 6 位
     */
    fun shortVersionNO(): String {
        if (null == versionNO) {
            return ""
        }
        return versionNO!!.substring(versionNO!!.length - 6)
    }

    /**
     * 截取修改原因前10位
     */
    fun shortReason(): String {
        if (null == modifyReason) {
            return ""
        }
        val r = modifyReason!!.substring(0, min(modifyReason!!.length, 10))
        return RegexUtil.removeIllegal(r)
    }

    /**
     * 转为 Po 对象
     */
    fun convertPo(): ModifyItemPo {
        val vo = this
        return ModifyItemPo().apply {
            absolutePath = vo.absolutePath
            modifyNum = vo.modifyNum
            timestamp = vo.timestamp
            desc = vo.modifyReason?.substring(0, min(20, vo.modifyReason!!.length))
            versionNo = vo.versionNO
            reqNums = vo.reqNums
            modifyReason = vo.modifyReason
        }

    }
}
