package indi.nonoas.worktools.view.env

import cn.hutool.db.Db
import cn.hutool.db.Entity

/**
 * TODO 类描述
 *
 * @author huangshengsheng
 * @date 2024/5/24 13:33
 */
class EnvVarDao{
    fun insert(envVar: EnvVar) {
        Db.use().insertOrUpdate(Entity.parse(envVar, true, true))
    }
}
