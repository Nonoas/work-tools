package indi.nonoas.worktools.utils

import cn.hutool.db.Db
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import indi.nonoas.worktools.config.DBConfigEnum
import java.sql.*
import java.util.*
import javax.sql.DataSource

/**
 * 数据库链接工具
 * @author Nonoas
 * @date 2021/9/17
 */
object DBUtil {

    private lateinit var ds: DataSource

    fun init(){
        val config = HikariConfig()
        config.jdbcUrl = DBConfigEnum.WORKTOOLS.url
        config.username = DBConfigEnum.WORKTOOLS.username
        config.password = DBConfigEnum.WORKTOOLS.password
        ds = HikariDataSource(config)
    }

    fun use(): Db {
        return Db.use(ds)
    }

    fun getConnection(): Connection {
        return ds.connection
    }

    /**
     * 执行更新语句 update 或 delete
     * @param ps PreparedStatement对象
     * @param args 可变参数
     */
    fun executeUpdate(ps: PreparedStatement, vararg args: Any): Int {
        for (i in args.indices) {
            ps.setObject(i + 1, args[i])
        }
        return ps.executeUpdate()
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