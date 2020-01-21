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

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CronScheduleRequest extends ScheduleRequest {
	private String cronString;
	private String misfireInstruction;

}
