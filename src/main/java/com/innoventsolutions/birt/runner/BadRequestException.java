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
