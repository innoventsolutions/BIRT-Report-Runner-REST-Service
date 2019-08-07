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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.quartz.JobKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SchedulerService {
	private final Map<JobKey, UUID> running = new HashMap<>();

	@Autowired
	public SchedulerService() {
	}

	public void addRunning(final JobKey jobKey, final UUID uuid) {
		this.running.put(jobKey, uuid);
	}
}
