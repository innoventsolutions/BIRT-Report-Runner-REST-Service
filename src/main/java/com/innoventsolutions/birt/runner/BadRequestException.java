/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.birt.runner;

import org.springframework.http.HttpStatus;

public class BadRequestException extends Exception {
	private static final long serialVersionUID = 1L;
	private final HttpStatus code;
	private final String reason;

	public BadRequestException(final HttpStatus code, final String reason) {
		this.code = code;
		this.reason = reason;
	}

	@Override
	public String getMessage() {
		return reason;
	}

	public HttpStatus getCode() {
		return code;
	}

	public String getReason() {
		return reason;
	}
}
