package indi.nonoas.worktools.dao

import cn.hutool.db.Db
import cn.hutool.db.Entity
import indi.nonoas.worktools.pojo.po.RtpLinkListPo

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

    fun delById(id: String): Int {
        return Db.use().del(TABLE_NAME, "id", id)
    }
}
