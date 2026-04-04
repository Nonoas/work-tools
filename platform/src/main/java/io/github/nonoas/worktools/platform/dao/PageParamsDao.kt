package io.github.nonoas.worktools.platform.dao

import io.github.nonoas.worktools.platform.pojo.dto.PageParamsDto
import io.github.nonoas.worktools.platform.pojo.po.PageParamsPo
import io.github.nonoas.worktools.platform.pojo.vo.PageParamsVo
import io.github.nonoas.worktools.platform.utils.DBUtil
import java.sql.Connection

/**
 * @author Nonoas
 * @date 2022/1/6
 */
object PageParamsDao {
    /**
     * 根据主键删除
     */
    fun deleteById(id: Long) {
        DBUtil.executeUpdate("delete from page_params where id = ?", id)
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
        val sql = """
            merge into page_params (param_code, param_val, last_use_timestamp)
            key (param_code, param_val)
            values (?, ?, ?);
            """
        return DBUtil.executeUpdate(sql, dto.paramCode, dto.paramVal, dto.lastUseTimestamp)
    }

    /**
     * 插入一条记录，通过 param_code 比较，
     * 存在则更新，否则插入
     */
    fun replaceIntoByParamCode(dto: PageParamsDto): Int {
        val sql = """
            merge into page_params (param_code, param_val, last_use_timestamp)
            key (param_code)
            values (?, ?, ?);
            """
        return DBUtil.executeUpdate(sql, dto.paramCode, dto.paramVal, dto.lastUseTimestamp)
    }
}