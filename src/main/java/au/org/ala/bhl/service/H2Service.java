package au.org.ala.bhl.service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;

import org.apache.commons.lang.StringUtils;

public abstract class H2Service {

	private String _path;
	private Connection _connection;

	protected H2Service(String path) {
		_path = path;
		log("Creating database connection");
		_connection = createConnection();
		log("Initializing service...");
		init();
		log("Initialization complete.");
	}

	protected void log(String format, Object... args) {
		LogService.log(H2Service.class, format, args);
	}

	protected abstract void init();

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

	protected void nonQuery(String sql, final Object... params) {
		statement(sql, new StatementHandler() {
			public void withStatement(PreparedStatement stmt) throws SQLException {
				setParameters(stmt, params);
				stmt.execute();
			}
		}, params);
	}

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

	protected void statement(String sql, StatementHandler handler, Object... params) {
		log("Preparing Statement: %s (%s)", sql, StringUtils.join(params, ", "));
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

	protected Connection createConnection() {
		try {
			Class.forName("org.h2.Driver");
			Connection conn = DriverManager.getConnection(String.format("jdbc:h2:%s", _path));
			return conn;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

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

	protected Date now() {
		return new Date(Calendar.getInstance().getTime().getTime());
	}

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

	public interface StatementHandler {

		void withStatement(PreparedStatement stmt) throws SQLException;

	}

	public interface ResultSetHandler {
		void onRow(ResultSet rs) throws SQLException;
	}

}
