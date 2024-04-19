package indi.nonoas.worktools.dao;

import java.sql.Connection;

/**
 * @author Nonoas
 * @date 2022/1/6
 */
public abstract class BaseDao {

	private final Connection conn;

	public BaseDao(Connection conn) {
		this.conn = conn;
	}

	public Connection getConnection() {
		return this.conn;
	}
}
