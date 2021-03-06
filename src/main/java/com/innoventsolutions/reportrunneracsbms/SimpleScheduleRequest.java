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

public class SimpleScheduleRequest extends ScheduleRequest {
	private Long intervalInMilliseconds;
	private Integer repeatCount;
	private String misfireInstruction;

	public Long getIntervalInMilliseconds() {
		return intervalInMilliseconds;
	}

	public void setIntervalInMilliseconds(final Long intervalInMilliseconds) {
		this.intervalInMilliseconds = intervalInMilliseconds;
	}

	public Integer getRepeatCount() {
		return repeatCount;
	}

	public void setRepeatCount(final Integer repeatCount) {
		this.repeatCount = repeatCount;
	}

	public String getMisfireInstruction() {
		return misfireInstruction;
	}

	public void setMisfireInstruction(final String misfireInstruction) {
		this.misfireInstruction = misfireInstruction;
	}
}
