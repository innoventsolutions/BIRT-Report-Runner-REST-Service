/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.brr.entity;

public class WaitforRequest extends StatusRequest {
	private Long timeout;

	public Long getTimeout() {
		return timeout;
	}

	public void setTimeout(final Long timeout) {
		this.timeout = timeout;
	}
}
