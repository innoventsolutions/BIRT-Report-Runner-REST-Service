/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.reportrunneracsbms;

import java.sql.SQLException;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import com.innoventsolutions.birt.runner.BadRequestException;

@Service
public class AuthorizationService {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	final ConfigService configService;

	@Autowired
	public AuthorizationService(final ConfigService configService) {
		this.configService = configService;
	}

	public void authorize(final String securityToken, final String requestDesignFile)
			throws BadRequestException, SQLException {
		if (configService.dbDriver == null) {
			logger.info("There is no dbDriver");
			// If there is no database, all reports are authorized
			return;
		}
		final DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(configService.dbDriver);
		dataSource.setUrl(configService.dbUrl);
		dataSource.setUsername(configService.dbUsername);
		dataSource.setPassword(configService.dbPassword);
		final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		if (securityToken == null) {
			throw new BadRequestException(HttpStatus.UNAUTHORIZED,
					"Security token is missing from request");
		}
		Authorization authorization;
		try {
			authorization = jdbcTemplate.queryForObject(configService.dbQuery,
				new Object[] { securityToken },
				(rs, rowNum) -> new Authorization(rs.getString(1), rs.getTimestamp(2)));
		}
		catch (final IncorrectResultSizeDataAccessException e) {
			if (e.getActualSize() == 0) {
				throw new BadRequestException(HttpStatus.UNAUTHORIZED, "Invalid security token");
			}
			throw new BadRequestException(HttpStatus.UNAUTHORIZED,
					"Security token matched more than one authorization");
		}
		if (authorization.designFile != null
			&& !authorization.designFile.equals(requestDesignFile)) {
			throw new BadRequestException(HttpStatus.UNAUTHORIZED, "Wrong design file");
		}
		if (authorization.submitTime != null && System.currentTimeMillis()
			- authorization.submitTime.getTime() > configService.dbTimeout) {
			throw new BadRequestException(HttpStatus.UNAUTHORIZED, "Security token has timed out");
		}
		// the security token was found, the design file matches, and we are inside
		// the time window so we are authorized to make this request
	}

	private static class Authorization {
		final String designFile;
		final Timestamp submitTime;

		public Authorization(final String designFile, final Timestamp timestamp) {
			this.designFile = designFile;
			this.submitTime = timestamp;
		}
	}
}
