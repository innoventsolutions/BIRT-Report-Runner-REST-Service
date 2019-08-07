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

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import com.innoventsolutions.birt.runner.ReportEmail;
import com.innoventsolutions.birt.runner.ReportRun;

public class RunReportQuartzJob implements Job {
	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		final JobDetail jobDetail = context.getJobDetail();
		final JobKey jobKey = jobDetail.getKey(); // identifies the job (name+group)
		final JobDataMap jobDataMap = context.getMergedJobDataMap();
		final ScheduledSubmitRequest request = (ScheduledSubmitRequest) jobDataMap.get(
			"submitRequest");
		if (request == null) {
			throw new RuntimeException("submitRequest not found in jobDataMap");
		}
		final RunnerService runner = (RunnerService) jobDataMap.get("runnerService");
		if (runner == null) {
			throw new RuntimeException("runnerService not found in jobDataMap");
		}
		final SchedulerService scheduler = (SchedulerService) jobDataMap.get("schedulerService");
		if (scheduler == null) {
			throw new RuntimeException("schedulerService not found in jobDataMap");
		}
		final UUID uuid = UUID.randomUUID();
		try {
			final String format = runner.getFormat(request.getFormat());
			final String outputFilename = uuid + "." + format;
			final ReportRun reportRun = new ReportRun(request.getDesignFile(),
					request.getNameForHumans(), format, outputFilename, request.isRunThenRender(),
					runner.fixParameterTypes(request.getParameters()), null);
			final ReportEmail email = new ReportEmail(request.isSendEmailOnSuccess(),
					request.isSendEmailOnFailure(), request.getMailTo(), request.getMailCc(),
					request.getMailBcc(), request.getMailSuccessSubject(),
					request.getMailFailureSubject(), request.getMailSuccessBody(),
					request.getMailFailureBody(), request.getMailAttachReport(),
					request.getMailHtml());
			final UUID jobUUID = runner.startReport(reportRun, email, false);
			scheduler.addRunning(jobKey, jobUUID);
		}
		catch (final Throwable e) {
			throw new JobExecutionException("Failed to submit report", e, false);
		}
	}
}
