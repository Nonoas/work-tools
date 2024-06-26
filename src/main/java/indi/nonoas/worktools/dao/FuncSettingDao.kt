package indi.nonoas.worktools.dao

import cn.hutool.core.util.StrUtil
import cn.hutool.db.Db
import cn.hutool.db.PageResult
import indi.nonoas.worktools.pojo.dto.FuncSettingDto
import indi.nonoas.worktools.pojo.params.FuncSettingQry
import indi.nonoas.worktools.pojo.po.FuncSettingPo
import indi.nonoas.worktools.pojo.vo.FuncSettingVo
import indi.nonoas.worktools.utils.DBUtil
import java.sql.Connection
import java.sql.ResultSet
import java.util.*

/**
 * @author Nonoas
 * @date 2022/1/24
 */
class FuncSettingDao(conn: Connection?) : BaseDao(conn) {

    fun pageBy(qry: FuncSettingQry): PageResult<FuncSettingPo>? {
        val sql = StringBuilder("select * from func_setting where 1=1")
        val params = HashMap<String, Any>()
        if (StrUtil.isNotBlank(qry.funcCode) && StrUtil.isNotBlank(qry.funcName)) {
            sql.append(
                """ 
                and ( upper(func_code)=upper(:func_code) 
                or upper(func_name) like '%'||upper(:func_name)||'%') 
                """
            )
            params["func_code"] = qry.funcCode!!
            params["func_name"] = qry.funcName!!
        }
        sql.append(" and enable_flag=:enable_flag ")
        params["enable_flag"] = qry.enableFlag

        sql.append(" limit :limit offset :offset")
        params["offset"] = (qry.pageNo * qry.pageSize)
        params["limit"] = qry.pageSize

        val result = DBUtil.use().query(sql.toString(), FuncSettingPo::class.java, params) ?: return null
        return PageResult<FuncSettingPo>().apply {
            addAll(result)
        }
    }

    /**
     * 获取全部数据
     */
    fun getAll(): ArrayList<FuncSettingDto> {
        val ps = connection.prepareStatement("select * from func_setting")
        val rs = ps.executeQuery()
        return resultToDtoList(rs)
    }

    /** 删除全部数据 */
    fun deleteAll(): Int {
        val ps = connection.prepareStatement("delete from func_setting where 1=1")
        return DBUtil.executeUpdate(ps)
    }

    /**
     * 批量插入数据
     */
    fun insertBatch(vos: List<FuncSettingVo>): IntArray {
        val ps = connection.prepareStatement(
            """
            insert into func_setting(func_code, func_name, enable_flag) VALUES (?,?,?);
            """
        )
        for (vo in vos) {
            ps.setString(1, vo.getFuncCode())
            ps.setString(2, vo.getFuncName())
            ps.setBoolean(3, vo.isEnableFlag())
            ps.addBatch()
        }
        return Optional.ofNullable(ps.executeBatch())
            .orElse(IntArray(0))
    }

    /**
     * 将 ResultSet 转换为 FuncSettingDto 集合
     */
    private fun resultToDtoList(rs: ResultSet): ArrayList<FuncSettingDto> {
        val list = ArrayList<FuncSettingDto>(8)
        var dtoTmp: FuncSettingDto?
        while (rs.next()) {
            dtoTmp = FuncSettingDto()
            list.add(dtoTmp.apply {
                funcCode = rs.getString("func_code")
                funcName = rs.getString("func_name")
                isEnableFlag = rs.getBoolean("enable_flag")
            })
        }
        return list
    }

}