package indi.nonoas.worktools.utils

import cn.hutool.db.Db
import cn.hutool.db.DbUtil
import cn.hutool.log.level.Level
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import indi.nonoas.worktools.config.DBConfigEnum
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import javax.sql.DataSource

/**
 * 数据库链接工具
 * @author Nonoas
 * @date 2021/9/17
 */
object DBUtil {

    private lateinit var ds: DataSource

    fun init() {
        // 数据源配置
        val config = HikariConfig()
        config.jdbcUrl = DBConfigEnum.WORKTOOLS.url
        config.username = DBConfigEnum.WORKTOOLS.username
        config.password = DBConfigEnum.WORKTOOLS.password
        config.maximumPoolSize = 5
        ds = HikariDataSource(config)
        // 设置全局SQL日志配置
        DbUtil.setShowSqlGlobal(true, false, true, Level.DEBUG)
    }

    fun use(): Db {
        return Db.use(ds)
    }

    fun getConnection(): Connection {
        return ds.connection
    }

    inline fun <T> useConnection(block: (Connection) -> T): T {
        getConnection().use {
            return block(it)
        }
    }

    inline fun <T> DBUtil.withTransaction(block: (Connection) -> T): T {
        return useConnection { conn ->
            val oldAutoCommit = conn.autoCommit
            try {
                conn.autoCommit = false
                val result = block(conn)
                conn.commit()
                result
            } catch (ex: Exception) {
                conn.rollback()
                throw ex
            } finally {
                conn.autoCommit = oldAutoCommit // 恢复原始状态，避免影响池子
            }
        }
    }


    /**
     * 执行更新语句 update 或 delete
     * @param ps PreparedStatement对象
     * @param args 可变参数
     */
    fun executeUpdate(ps: PreparedStatement, vararg args: Any): Int {
        ps.use {
            for (i in args.indices) {
                ps.setObject(i + 1, args[i])
            }
            return ps.executeUpdate()
        }
    }

    /**
     * 执行查询语句 select
     * @param ps PreparedStatement对象
     * @param args 可变参数
     */
    fun executeQuery(ps: PreparedStatement, vararg args: Any): ResultSet {
        for (i in args.indices) {
            ps.setObject(i + 1, args[i])
        }
        return ps.executeQuery()
    }

}