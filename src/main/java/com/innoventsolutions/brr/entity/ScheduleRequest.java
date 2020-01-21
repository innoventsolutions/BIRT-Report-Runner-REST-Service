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

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
abstract public class ScheduleRequest extends BaseRequest {
	private String group;
	private String name;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date startDate;
	private ScheduledSubmitRequest submit;

}
