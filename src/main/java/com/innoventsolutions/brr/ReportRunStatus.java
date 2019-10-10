/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.brr;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.innoventsolutions.brr.entity.ReportEmail;

public class ReportRunStatus {
	public final ReportRun reportRun;
	public final ReportEmail email;
	public final Date startTime;
	private Date reportFinishTime = null;
	private Date finishTime = null;
	private final List<Exception> errors = new ArrayList<>();
	private final Map<String, Exception> emailErrors = new HashMap<>();

	public ReportRunStatus(final ReportRun reportRun, final ReportEmail email) {
		this.reportRun = reportRun;
		this.email = email;
		this.startTime = new Date();
	}

	public void finishReport(final List<Exception> errors) {
		reportFinishTime = new Date();
		if (errors != null) {
			this.errors.addAll(errors);
		}
	}

	public synchronized void finishEmail(final Map<String, Exception> errors) {
		// issue 10 - set finish time inside synchronization
		finishTime = new Date();
		if (errors != null) {
			this.emailErrors.putAll(errors);
		}
		this.notifyAll();
	}

	public boolean isFinished() {
		return finishTime != null;
	}

	public Date getFinishTime() {
		return finishTime;
	}

	public List<Exception> getErrors() {
		return new ArrayList<>(errors);
	}

	public Date getReportFinishTime() {
		return reportFinishTime;
	}

	public String replace(String string) {
		final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		string = string.replace("${designFileName}", reportRun.designFile);
		string = string.replace("${nameForHumans}", reportRun.nameForHumans);
		string = string.replace("${startTime}", df.format(startTime));
		final Date finishTime = this.finishTime;
		string = string.replace("${finishTime}", finishTime != null ? df.format(finishTime) : "");
		return string;
	}

	public long getDuration() {
		final Date finishTime = this.finishTime;
		final long finishTimeLong = finishTime == null ? System.currentTimeMillis()
			: finishTime.getTime();
		final long startTimeLong = startTime.getTime();
		return finishTimeLong - startTimeLong;
	}

	public ReportRun getReportRun() {
		return reportRun;
	}

	public ReportEmail getEmail() {
		return email;
	}

	public Date getStartTime() {
		return startTime;
	}

	public Map<String, Exception> getEmailErrors() {
		return emailErrors;
	}
}
