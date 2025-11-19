package indi.nonoas.worktools.view.env

import cn.hutool.db.Db
import cn.hutool.db.Entity
import indi.nonoas.worktools.utils.DBUtil

/**
 * TODO 类描述
 *
 * @author huangshengsheng
 * @date 2024/5/24 13:33
 */
object EnvVarDao{
    fun insert(envVar: EnvVar) {
        DBUtil.use().insertOrUpdate(Entity.parse(envVar, true, true))
    }

    fun queryByName(name: String): MutableList<EnvVar> {
        return DBUtil.use().query("select * from env_var where name=?", EnvVar::class.java, name)
    }
}
