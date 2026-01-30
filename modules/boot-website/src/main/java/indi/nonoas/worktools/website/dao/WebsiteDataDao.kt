package indi.nonoas.worktools.website.dao

import indi.nonoas.worktools.platform.utils.DBUtil
import indi.nonoas.worktools.website.BootWebsitePane
import java.sql.ResultSet

/**
 * WebsiteData 数据访问对象
 */
object WebsiteDataDao {

    /**
     * 增/改：如果 command 存在则更新，不存在则插入
     */
    fun saveOrUpdate(data: BootWebsitePane.WebsiteData): Int {
        val sql = "MERGE INTO website_data (command, url, alias) KEY(command) VALUES (?, ?, ?)"
        return DBUtil.executeUpdate(sql, data.command.get(), data.url.get(), data.alias.get())
    }

    /**
     * 删：根据 command 删除
     */
    fun deleteByCommand(command: String): Int {
        val sql = "DELETE FROM website_data WHERE command = ?"
        return DBUtil.executeUpdate(sql, command)
    }

    /**
     * 查：获取所有列表
     */
    fun findAll(): List<BootWebsitePane.WebsiteData> {
        val sql = "SELECT * FROM website_data"
        val list = mutableListOf<BootWebsitePane.WebsiteData>()
        
        DBUtil.useConnection { conn ->
            conn.prepareStatement(sql).use { ps ->
                val rs = ps.executeQuery()
                while (rs.next()) {
                    list.add(mapRow(rs))
                }
            }
        }
        return list
    }

    /**
     * 查：根据 command 获取单个对象
     */
    fun findByCommand(command: String): BootWebsitePane.WebsiteData? {
        val sql = "SELECT * FROM website_data WHERE command = ?"
        return DBUtil.useConnection { conn ->
            conn.prepareStatement(sql).use { ps ->
                ps.setString(1, command)
                val rs = ps.executeQuery()
                if (rs.next()) mapRow(rs) else null
            }
        }
    }

    /**
     * 内部行映射逻辑
     */
    private fun mapRow(rs: ResultSet): BootWebsitePane.WebsiteData {
        return BootWebsitePane.WebsiteData(
            rs.getString("command") ?: "",
            rs.getString("url") ?: "",
            rs.getString("alias") ?: ""
        )
    }
}