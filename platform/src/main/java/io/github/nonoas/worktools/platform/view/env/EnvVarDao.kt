package io.github.nonoas.worktools.platform.view.env

import cn.hutool.db.Entity
import io.github.nonoas.worktools.platform.utils.DBUtil

/**
 * 环境变量持久化访问对象。
 */
object EnvVarDao {

    private const val TABLE_NAME = "env_var"

    /**
     * 新增一条环境变量记录。
     *
     * @param envVar 待保存的环境变量对象
     */
    fun insert(envVar: EnvVar) {
        save(envVar)
    }

    /**
     * 保存环境变量记录。
     * 当 [EnvVar.id] 存在时执行更新，否则执行新增。
     *
     * @param envVar 待保存的环境变量对象
     */
    fun save(envVar: EnvVar) {
        val entity = Entity.parse(envVar, true, true)
        entity.tableName = TABLE_NAME
        if (envVar.id == null) {
            DBUtil.use().insert(entity)
            return
        }
        DBUtil.use().insertOrUpdate(entity, "id")
    }

    /**
     * 按环境变量名查询全部记录。
     *
     * @param name 环境变量名
     * @return 同名环境变量列表，按最近修改时间倒序排列
     */
    fun queryByName(name: String): MutableList<EnvVar> {
        return DBUtil.use().query(
            "select * from env_var where name=? order by mod_timestamp desc, create_timestamp desc",
            EnvVar::class.java,
            name
        )
    }

    /**
     * 按主键删除环境变量记录。
     *
     * @param id 记录主键
     * @return 删除条数
     */
    fun deleteById(id: Int): Int {
        return DBUtil.use().del(TABLE_NAME, "id", id)
    }
}
