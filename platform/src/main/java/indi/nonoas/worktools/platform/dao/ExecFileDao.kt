package indi.nonoas.worktools.platform.dao

import cn.hutool.db.Entity
import indi.nonoas.worktools.platform.pojo.vo.ExecFileVo
import indi.nonoas.worktools.platform.utils.DBUtil

/**
 *
 * @version
 * @since
 * @author Nonoas
 * @date 2024/6/2
 */
object ExecFileDao {
    private const val TABLE_NAME = "EXEC_FILE"

    fun insertBatch(params: MutableList<Array<Any>>): IntArray? =
        DBUtil.use().executeBatch(
            """
              insert into ${TABLE_NAME} (NAME,LINk,CREATE_TIMESTAMP,LAST_USE_TIMESTAMP)
              VALUES ( ?,?,?,? );
            """.trimIndent(), params
        )


    fun findAll(): List<ExecFileVo> = DBUtil.use().findAll(Entity.create(TABLE_NAME), ExecFileVo::class.java)

    fun search(keyword: String?): MutableList<ExecFileVo> = DBUtil.use().query(
        """
            select * from $TABLE_NAME where upper(name) like '%'||upper(?)||'%'
        """.trimIndent(),
        ExecFileVo::class.java, keyword
    )

    fun delByUniqueKey(vo: ExecFileVo): Int {
        if (vo.id == 0L) {
            return DBUtil.use().del(TABLE_NAME, "link", vo.link)
        }
        return DBUtil.use().del(TABLE_NAME, "id", vo.id)
    }
}