/*******************************************************************************
 * Copyright (C) 2011 Atlas of Living Australia
 * All Rights Reserved.
 *   
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *   
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ******************************************************************************/
package au.org.ala.bhl.service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;

/**
 * Base class for services that are backed by a H2 database
 * @author baird
 *
 */
public abstract class H2Service extends AbstractService {

	private String _path;
	private Connection _connection;

	protected H2Service(String path) {
		_path = path;
		_connection = createConnection();
		init();
	}

	protected abstract void init();

	/**
	 * Executes a query, and invokes the handler for each row returned
	 * 
	 * @param sql
	 * @param handler
	 * @param params
	 */
	protected void queryForEach(final String sql, final ResultSetHandler handler, final Object... params) {
		statement(sql, new StatementHandler() {
			public void withStatement(PreparedStatement stmt) throws SQLException {
				setParameters(stmt, params);
				ResultSet rs = stmt.executeQuery();
				try {
					while (rs.next()) {
						if (handler != null) {
							handler.onRow(rs);
						}
					}
				} finally {
					if (rs != null && !rs.isClosed()) {
						rs.close();
					}
				}
			}
		}, params);

	}

	/**
	 * Executes a non-query statement (update, insert, delete etc).
	 * @param sql
	 * @param params
	 */
	protected void nonQuery(String sql, final Object... params) {
		statement(sql, new StatementHandler() {
			public void withStatement(PreparedStatement stmt) throws SQLException {
				setParameters(stmt, params);
				stmt.execute();
			}
		}, params);
	}

	/**
	 * Helper used to set parameter values on parameterised statements
	 * 
	 * @param stmt
	 * @param params
	 * @throws SQLException
	 */
	private void setParameters(PreparedStatement stmt, Object[] params) throws SQLException {
		for (int i = 0; i < params.length; ++i) {
			Object param = params[i];
			if (param == null) {
				stmt.setNull(i + 1, Types.VARCHAR);
			} else if (param instanceof String) {
				stmt.setString(i + 1, (String) param);
			} else if (param instanceof Integer) {
				stmt.setInt(i + 1, (Integer) param);
			} else if (param instanceof Double) {
				stmt.setDouble(i + 1, (Double) param);
			} else if (param instanceof Date) {
				stmt.setDate(i + 1, (Date) param);
			} else {
				throw new RuntimeException("Unhandled paramter type!");
			}
		}

	}

	/**
	 * Execute an update statement
	 * 
	 * @param sql
	 * @param params
	 * @return
	 */
	protected int update(String sql, final Object... params) {
		final int[] result = new int[1];
		statement(sql, new StatementHandler() {
			public void withStatement(PreparedStatement stmt) throws SQLException {
				setParameters(stmt, params);
				result[0] = stmt.executeUpdate();
			}
		}, params);
		return result[0];
	}

	/**
	 * Creates a statement that is passed to handler before being correctly closed
	 * 
	 * @param sql
	 * @param handler
	 * @param params
	 */
	protected void statement(String sql, StatementHandler handler, Object... params) {
		// log("Preparing Statement: %s (%s)", sql, StringUtils.join(params, ", "));
		Connection con = getConnection();
		PreparedStatement stmt = null;
		try {
			stmt = con.prepareStatement(sql);
			if (handler != null) {
				handler.withStatement(stmt);
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		}
	}

	/**
	 * Create a new connection to the database
	 * @return
	 */
	protected Connection createConnection() {
		try {
			Class.forName("org.h2.Driver");
			Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", _path));
			return conn;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * get a persistent connection
	 * @return
	 */
	protected Connection getConnection() {
		try {
			if (_connection == null || _connection.isClosed()) {
				_connection = createConnection();
			}
			return _connection;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/** 
	 * @return the current date and time
	 */
	protected Date now() {
		return new Date(Calendar.getInstance().getTime().getTime());
	}

	/**
	 * Disconnect from the service. Persistent connections will be closed
	 */
	public void disconnect() {
		try {
			log("Disconnecting from H2Service");
			if (_connection != null && !_connection.isClosed()) {
				_connection.close();
			}
		} catch (SQLException sqlex) {
			sqlex.printStackTrace();
		}
	}

	/**
	 * Handler interface for dealing with statements
	 *
	 */
	public interface StatementHandler {
		void withStatement(PreparedStatement stmt) throws SQLException;
	}

	/**
	 * Handler interface for dealing with result sets
	 * 
	 */
	public interface ResultSetHandler {
		void onRow(ResultSet rs) throws SQLException;
	}

}
