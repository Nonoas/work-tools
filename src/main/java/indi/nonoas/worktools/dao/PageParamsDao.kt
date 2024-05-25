package indi.nonoas.worktools.dao

import cn.hutool.db.Db
import indi.nonoas.worktools.pojo.dto.PageParamsDto
import indi.nonoas.worktools.pojo.po.PageParamsPo
import indi.nonoas.worktools.pojo.vo.PageParamsVo
import indi.nonoas.worktools.utils.DBUtil
import java.sql.Connection

/**
 * @author Nonoas
 * @date 2022/1/6
 */
class PageParamsDao(conn: Connection?) : BaseDao(conn) {
    /**
     * 根据主键删除
     */
    fun deleteById(id: Long) {
        val ps = connection.prepareStatement("delete from page_params where id = ?")
        DBUtil.executeUpdate(ps, id)
    }

    /**
     * 通过 ParamCode 获取
     */
    fun getByParamCode(code: String): List<PageParamsVo> {
        return DBUtil.use().query(
            "select * from page_params where param_code = ? order by last_use_timestamp desc",
            PageParamsPo::class.java,
            code
        ).map(PageParamsPo::convertVo)
    }

    /**
     * 插入一条记录，通过 param_code, param_val 比较，
     * 存在则更新，否则插入
     */
    fun replaceInto(dto: PageParamsDto): Int {
        val ps = connection.prepareStatement(
            """
            merge into page_params (param_code, param_val, last_use_timestamp)
            key (param_code, param_val)
            values (?, ?, ?);
            """
        )
        return DBUtil.executeUpdate(ps, dto.paramCode, dto.paramVal, dto.lastUseTimestamp)
    }

    /**
     * 插入一条记录，通过 param_code 比较，
     * 存在则更新，否则插入
     */
    fun replaceIntoByParamCode(dto: PageParamsDto): Int {
        val ps = connection.prepareStatement(
            """
            merge into page_params (param_code, param_val, last_use_timestamp)
            key (param_code)
            values (?, ?, ?);
            """
        )
        return DBUtil.executeUpdate(ps, dto.paramCode, dto.paramVal, dto.lastUseTimestamp)
    }
}