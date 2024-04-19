package indi.nonoas.worktools.utils

import java.sql.*
import java.util.*

/**
 * 数据库链接工具
 * @author Nonoas
 * @date 2021/9/17
 */
object DBUtil {

    private var connection: Connection? = null

    private const val URL = "jdbc:h2:./db/worktools"
    private const val USERNAME = "worktools"
    private const val PASSWORD = "worktools"

    fun getConnection(): Connection {
        /*fixme 当前获取链接为单例获取，可能导致多任务执行时的事务冲突，需要修复
        *  可以考虑连接池模式 */
        if (null == connection || connection!!.isClosed) {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)
        }
        return Optional.ofNullable(connection)
            .orElseThrow { SQLException("获取数据库连接失败") }
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