package indi.nonoas.worktools.dao

import cn.hutool.db.Db
import cn.hutool.db.Entity
import indi.nonoas.worktools.pojo.po.RtpLinkListPo
import indi.nonoas.worktools.pojo.vo.RtpLinkListVo

/**
 * RtpLinkListDao
 *
 * @author huangshengsheng
 * @date 2024/5/16 10:09
 */
class RtpLinkListDao {

    val TABLE_NAME = "RTP_LINKLIST"

    fun add(po: RtpLinkListPo): Int {
        val entity = Entity.parse(po, true, true)
        entity.tableName = TABLE_NAME
        return Db.use().insert(entity)
    }

    fun replace(vo: RtpLinkListVo) {
        val entity = Entity.parse(vo.covertPo(), true, true)
        entity.tableName = TABLE_NAME
        Db.use().insertOrUpdate(entity, "id")
    }

    fun delById(id: String): Int {
        return Db.use().del(TABLE_NAME, "id", id)
    }

    fun getAll(): MutableList<RtpLinkListPo> {
        return Db.use().query("select id,name,link,last_use_timestamp " +
                "from rtp_linklist order by last_use_timestamp desc",
                RtpLinkListPo::class.java)
    }
}
