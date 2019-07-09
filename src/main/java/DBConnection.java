
import java.io.Closeable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * @author Collin Alpert
 * @see <a href="https://github.com/CollinAlpert/APIs/blob/master/de/collin/DBConnection.java">GitHub</a>
 *
 * modified by Malte Schink
 */
public class DBConnection implements Closeable {
	private static String HOSTNAME = null;  //Specifies the hostname/ip address of the database.
	private static String DATABASE = null;  //Specifies the name of the database to connect to.
	private static String USERNAME = null;  //Specifies the username to log in on the database with.
	private static String PASSWORD = null;  //Specifies the password to log in on the database with.

	/**
	 * Specifies the port to connect to the database on.
	 * This property is optional. If not specified, it will be set to 3306, the default port of MySQL.
	 */
	public static int PORT = 3306;


	static {
		DriverManager.setLoginTimeout(5);
	}

	private Connection connection;
	private boolean isConnectionValid;

	public DBConnection() {
		try {
			Properties config = Config.load(Config.PATH_CONFIG);
			HOSTNAME = config.getProperty("hostname");
			DATABASE = config.getProperty("database");
			USERNAME = config.getProperty("username");
			PASSWORD = config.getProperty("password");
			
			DriverManager.setLoginTimeout(5);
			connection = DriverManager.getConnection(
							"jdbc:mariadb://" + HOSTNAME + ":" + PORT + "/" + DATABASE + "?autoReconnect=true&serverTimezone=UTC",
							USERNAME,
							PASSWORD);
			
			isConnectionValid = true;
		} catch ( SQLException e) {
			e.printStackTrace();
			isConnectionValid = false;
		} catch (Exception e) {
			isConnectionValid = false;
		}
	}

	/**
	 * Checks if the connection is valid/successful.
	 *
	 * @return True if connection was successful, false if not.
	 */
	public boolean isValid() {
		return this.isConnectionValid;
	}


	/**
	 * Executes a DQL statement on the database without Java parameters.
	 *
	 * @param query The query to be executed.
	 * @return The {@link ResultSet} containing the result from the DQL statement.
	 * @throws SQLException if the query is malformed or cannot be executed.
	 */
	public ResultSet execute(String query) throws SQLException {
		Statement statement = connection.createStatement();
		//LocalLog.status(query);
		var set = statement.executeQuery(query);
		statement.closeOnCompletion();
		return set;
	}

	/**
	 * Executes a DQL statement on the database with Java parameters.
	 *
	 * @param query  The query to be executed.
	 * @param params The Java parameters to be inserted into the query.
	 * @return The {@link ResultSet} containing the result from the DQL statement.
	 * @throws SQLException if the query is malformed or cannot be executed.
	 */
	public ResultSet execute(String query, Object... params) throws SQLException {
		var statement = connection.prepareStatement(query);
		for (int i = 0; i < params.length; i++) {
			statement.setObject(i + 1, params[i]);
		}

		//LocalLog.status(query);
		var set = statement.executeQuery();
		statement.closeOnCompletion();
		return set;
	}

	/**
	 * This command is used for any DDL/DML queries.
	 *
	 * @param query The query to be executed.
	 * @return the last generated ID. This return value should only be used with INSERT statements.
	 * @throws SQLException if the query is malformed or cannot be executed.
	 */
	public long update(String query) throws SQLException {
		var statement = connection.createStatement();
		//LocalLog.status(query);
		statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
		return updateHelper(statement);
	}

	/**
	 * This command is used for any DDL/DML queries with Java parameters.
	 *
	 * @param query  The query to be executed.
	 * @param params The Java parameters to be inserted into the query.
	 * @return the last generated ID. This return value should only be used with INSERT statements.
	 * @throws SQLException if the query is malformed or cannot be executed.
	 */
	public long update(String query, Object... params) throws SQLException {
		var statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
		for (int i = 0; i < params.length; i++) {
			statement.setObject(i + 1, params[i]);
		}

		//LocalLog.status(query);
		statement.executeUpdate();
		return updateHelper(statement);
	}

	private long updateHelper(Statement statement) throws SQLException {
		statement.closeOnCompletion();
		var set = statement.getGeneratedKeys();
		if (set.next()) {
			return set.getLong(1);
		}

		return -1;
	}

	/**
	 * Determines if a connection to the database still exists or not.
	 *
	 * @return {@code True} if a connection exists, {@code false} if not.
	 * This method will return {@code false} if an exception occurs.
	 */
	public boolean isOpen() {
		try {
			return !connection.isClosed();
		} catch (SQLException e) {
			System.err.println("Could not determine connection status");
			return isConnectionValid = false;
		}
	}

	/**
	 * Closes the connection to the database.
	 */
	@Override
	public void close() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			System.err.println("Could not close database connection");
			e.printStackTrace();
		} finally {
			isConnectionValid = false;
		}
	}
}
