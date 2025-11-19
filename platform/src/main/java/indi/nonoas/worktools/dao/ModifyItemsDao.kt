package indi.nonoas.worktools.dao

import cn.hutool.core.util.StrUtil
import cn.hutool.db.Db
import cn.hutool.db.Entity
import cn.hutool.db.Page
import cn.hutool.db.PageResult
import indi.nonoas.worktools.pojo.params.ModifyItemQry
import indi.nonoas.worktools.pojo.params.MyEntity
import indi.nonoas.worktools.pojo.po.ModifyItemPo
import indi.nonoas.worktools.utils.DBUtil

/**
 * 工作期间持久层
 * @author Nonoas
 * @date 2022/5/9
 */
class ModifyItemsDao {

    /**
     * 分页查询修改单
     */
    fun pagedBy(param: ModifyItemQry): PageResult<Entity>? {
        val entity = MyEntity.create("modify_items").apply {
            whereNonNull("workspace", param.workSpace)
            whereNonNull("modify_num", param.modifyNum)
            likeNonNull("modify_reason", param.modifyReason)
        }

        return DBUtil.use().page(
            entity,
            Page(param.pageNo, param.pageSize),
        )
    }

    /**
     * 分页查询修改单
     */
    fun pagedByKeyWords(qry: ModifyItemQry): PageResult<ModifyItemPo>? {

        val sql = StringBuilder("select * from modify_items where 1=1")
        val params = HashMap<String, Any>()
        // 单号和修改原因
        if (StrUtil.isNotBlank(qry.modifyNum) && StrUtil.isNotBlank(qry.modifyReason)) {
            sql.append(
                """ 
                and ( upper(modify_num)=upper(:modify_num) 
                or modify_reason like '%'||:modify_reason||'%') 
                """
            )
            params["modify_num"] = qry.modifyNum!!
            params["modify_reason"] = qry.modifyReason!!
        }
        // 工作区间
        if (StrUtil.isNotBlank(qry.workSpace)) {
            sql.append(" and workspace=:workspace ")
            params["workspace"] = qry.workSpace!!
        }

        val totalSql = sql.toString()
        val total = DBUtil.use().count(totalSql, params)

        sql.append("order by modify_time desc limit :limit offset :offset")
        params["offset"] = (qry.pageNo * qry.pageSize)
        params["limit"] = qry.pageSize

        val result = DBUtil.use().query(sql.toString(), ModifyItemPo::class.java, params) ?: return null
        return PageResult<ModifyItemPo>().apply {
            addAll(result)
            this.total = total.toInt()
            totalPage = ((total + qry.pageSize - 1) / qry.pageSize).toInt()
        }
    }

}