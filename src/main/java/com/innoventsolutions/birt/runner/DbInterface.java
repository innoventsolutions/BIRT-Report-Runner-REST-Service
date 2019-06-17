package com.innoventsolutions.birt.runner;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DbInterface {
	Logger logger = LoggerFactory.getLogger(DbInterface.class);
	private final ComboPooledDataSource pool;
	private final String query;
	private final Long timeout;

	public DbInterface(final Configuration configuration) throws SQLException {
		if (configuration.dbDriver == null) {
			logger.info("dbInterface is disabled because dbDriver property is missing");
			pool = null;
			query = null;
			timeout = null;
			return;
		}
		if (configuration.dbUrl == null) {
			logger.info("dbInterface is disabled because dbUrl property is missing");
			pool = null;
			query = null;
			timeout = null;
			return;
		}
		final ComboPooledDataSource pool = new ComboPooledDataSource();
		try {
			pool.setDriverClass(configuration.dbDriver);
		}
		catch (final PropertyVetoException e) {
			// this is never thrown
		}
		pool.setJdbcUrl(configuration.dbUrl);
		pool.setUser(configuration.dbUsername);
		pool.setPassword(configuration.dbPassword);
		pool.setMaxStatements(10);
		this.pool = pool;
		this.query = configuration.dbQuery;
		this.timeout = configuration.dbTimeout;
	}

	public boolean enabled() {
		return pool != null;
	}

	public void authorize(final String securityToken, final String requestDesignFile)
			throws BadRequestException, SQLException {
		if (pool == null) {
			// If there is no database, all reports are authorized
			return;
		}
		if (securityToken == null) {
			throw new BadRequestException(HttpStatus.UNAUTHORIZED,
					"Security token is missing from request");
		}
		final Connection connection = pool.getConnection();
		try {
			final PreparedStatement statement = connection.prepareStatement(query);
			try {
				statement.setString(1, securityToken);
				final ResultSet resultSet = statement.executeQuery();
				try {
					if (!resultSet.next()) {
						throw new BadRequestException(HttpStatus.UNAUTHORIZED,
								"Invalid security token");
					}
					final String designFile = resultSet.getString(1);
					final Timestamp submitTime = resultSet.getTimestamp(2);
					if (designFile != null && !designFile.equals(requestDesignFile)) {
						throw new BadRequestException(HttpStatus.UNAUTHORIZED, "Wrong design file");
					}
					if (submitTime != null
						&& System.currentTimeMillis() - submitTime.getTime() > timeout) {
						throw new BadRequestException(HttpStatus.UNAUTHORIZED,
								"Security token has timed out");
					}
					// the security token was found, the design file matches, and we are inside
					// the time window so we are authorized to make this request
				}
				finally {
					resultSet.close();
				}
			}
			finally {
				statement.close();
			}
		}
		finally {
			connection.close();
		}
	}
}
