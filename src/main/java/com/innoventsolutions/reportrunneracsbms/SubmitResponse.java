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

import java.util.UUID;

public class SubmitResponse {
	private final Throwable exception;
	private final UUID uuid;

	public String getExceptionString() {
		return exception == null ? "" : exception.toString();
	}

	public boolean isSuccess() {
		return exception == null;
	}

	public UUID getUuid() {
		return uuid;
	}

	public SubmitResponse(final UUID uuid, final Throwable exception) {
		this.exception = exception;
		this.uuid = uuid;
	}

	public Throwable getException() {
		return exception;
	}
}
