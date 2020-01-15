/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.brr.service;

import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.innoventsolutions.brr.exception.BadRequestException;
import com.innoventsolutions.brr.jpa.dao.AuthorizationRepository;
import com.innoventsolutions.brr.jpa.model.Authorization;

@Service
public class AuthorizationService {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	final ConfigService configService;
	@Autowired
	private AuthorizationRepository authRepository;


	@Autowired
	public AuthorizationService(final ConfigService configService) {
		this.configService = configService;
	}

	public void authorize(final String securityToken, final String requestDesignFile)
			throws BadRequestException, SQLException {
		
		List<Authorization> as = (List<Authorization>)authRepository.findAll();
		List<Authorization> auths = authRepository.findAllBySecurityToken(securityToken);
		
		if (auths.size() < 1) {
			throw new BadRequestException(HttpStatus.UNAUTHORIZED, "No security Token: " + securityToken);
		}

		for (Authorization authorization : auths) {
			if (authorization.getDesignFile() != null && !authorization.getDesignFile().equals(requestDesignFile)) {
				throw new BadRequestException(HttpStatus.UNAUTHORIZED, "Wrong design file");
			}
			if (authorization.getSubmitTime() != null
					&& System.currentTimeMillis() - authorization.getSubmitTime().getTime() > configService.dbTimeout) {
				throw new BadRequestException(HttpStatus.UNAUTHORIZED, "Security token has timed out");
			}
			
		}
		// the security token was found, the design file matches, and we are inside
		// the time window so we are authorized to make this request
	}

}
