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

import java.util.Date;

abstract public class ScheduleRequest extends BaseRequest {
	private String group;
	private String name;
	private Date startDate;
	private ScheduledSubmitRequest submit;

	public String getGroup() {
		return group;
	}

	public void setGroup(final String group) {
		this.group = group;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public ScheduledSubmitRequest getSubmit() {
		return submit;
	}

	public void setSubmit(final ScheduledSubmitRequest submit) {
		this.submit = submit;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(final Date startDate) {
		this.startDate = startDate;
	}
}
