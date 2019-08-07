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

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.innoventsolutions.birt.runner.BadRequestException;
import com.innoventsolutions.birt.runner.ReportEmail;
import com.innoventsolutions.birt.runner.ReportRun;
import com.innoventsolutions.birt.runner.ReportRunStatus;

@Controller
public class JobController {
	Logger logger = LoggerFactory.getLogger(JobController.class);
	@Autowired
	private RunnerService runner;
	@Autowired
	private SchedulerService schedulerService;

	@GetMapping("/welcome")
	public String loginMessage(final Model model) {
		model.addAttribute("message", "test");
		return "welcome"; // view
	}

	@Autowired
	ConfigService configService;
	@Autowired
	AuthorizationService authorizationService;

	@GetMapping("/get")
	@ResponseBody
	public ResponseEntity<Resource> getReport(@RequestBody final StatusRequest request) {
		return getReport(request, false);
	}

	@GetMapping("/download")
	@ResponseBody
	public ResponseEntity<Resource> downloadReport(@RequestBody final StatusRequest request) {
		return getReport(request, true);
	}

	public ResponseEntity<Resource> getReport(final StatusRequest request,
			final boolean isAttachment) {
		try {
			logger.info("getReport " + request + " " + isAttachment);
			if (configService.unsecuredOperationPattern != null) {
				final Matcher matcher = configService.unsecuredOperationPattern.matcher(
					isAttachment ? "download" : "get");
				if (!matcher.matches()) {
					final String securityToken = request.getSecurityToken();
					try {
						authorizationService.authorize(securityToken, null);
					}
					catch (final BadRequestException e) {
						return new ResponseEntity<Resource>(e.getCode());
					}
					catch (final SQLException e) {
						return new ResponseEntity<Resource>(HttpStatus.INTERNAL_SERVER_ERROR);
					}
				}
			}
			final File outputDir = runner.getOutputDirectory();
			logger.info("outputDir = " + outputDir);
			UUID uuid;
			try {
				uuid = UUID.fromString(request.getJobId());
			}
			catch (final IllegalArgumentException e) {
				return getErrorResponse(HttpStatus.NOT_ACCEPTABLE, "Invalid or missing UUID");
			}
			final ReportRunStatus status = runner.getStatus(uuid);
			if (status == null) {
				return getErrorResponse(HttpStatus.NOT_FOUND, "Report not found");
			}
			while (!status.isFinished()) {
				logger.info("waiting...");
				synchronized (status) {
					try {
						status.wait();
					}
					catch (final InterruptedException e) {
					}
				}
				logger.info("done waiting");
			}
			if (!status.isFinished()) {
				return getErrorResponse(HttpStatus.BAD_REQUEST, "Report is not finished");
			}
			final File outputFile = outputDir == null ? new File(status.reportRun.outputFile)
				: new File(outputDir, status.reportRun.outputFile);
			final InputStream inputStream = new FileInputStream(outputFile);
			final HttpHeaders headers = new HttpHeaders();
			if (isAttachment) {
				headers.set("Content-Disposition",
					"attachment; filename=\"" + status.reportRun.outputFile + "\"");
			}
			final InputStreamResource resource = new InputStreamResource(inputStream);
			return ResponseEntity.ok().headers(headers).contentLength(
				outputFile.length()).contentType(getMediaType(status.reportRun.format)).body(
					resource);
		}
		catch (final Throwable e) {
			logger.error("Exception", e);
			return getErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.toString());
		}
	}

	@GetMapping("/status")
	@ResponseBody
	public ResponseEntity<ReportRunStatus> getStatus(@RequestBody final StatusRequest request) {
		if (configService.unsecuredOperationPattern != null) {
			final Matcher matcher = configService.unsecuredOperationPattern.matcher("status");
			if (!matcher.matches()) {
				final String securityToken = request.getSecurityToken();
				try {
					authorizationService.authorize(securityToken, null);
				}
				catch (final BadRequestException e) {
					return new ResponseEntity<ReportRunStatus>(e.getCode());
				}
				catch (final SQLException e) {
					return new ResponseEntity<ReportRunStatus>(HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}
		}
		UUID uuid;
		try {
			uuid = UUID.fromString(request.getJobId());
		}
		catch (final IllegalArgumentException e) {
			return new ResponseEntity<ReportRunStatus>(HttpStatus.NOT_ACCEPTABLE);
		}
		final ReportRunStatus status = runner.getStatus(uuid);
		return new ResponseEntity<ReportRunStatus>(status, HttpStatus.OK);
	}

	@GetMapping("/status-all")
	@ResponseBody
	public ResponseEntity<Map<UUID, ReportRunStatus>> getAllStati(
			@RequestBody final BaseRequest request) {
		if (configService.unsecuredOperationPattern != null) {
			final Matcher matcher = configService.unsecuredOperationPattern.matcher("status");
			if (!matcher.matches()) {
				final String securityToken = request.getSecurityToken();
				try {
					authorizationService.authorize(securityToken, null);
				}
				catch (final BadRequestException e) {
					return new ResponseEntity<Map<UUID, ReportRunStatus>>(e.getCode());
				}
				catch (final SQLException e) {
					return new ResponseEntity<Map<UUID, ReportRunStatus>>(
							HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}
		}
		final Map<UUID, ReportRunStatus> status = runner.getStati();
		return new ResponseEntity<Map<UUID, ReportRunStatus>>(status, HttpStatus.OK);
	}

	@GetMapping("/waitfor")
	@ResponseBody
	public ResponseEntity<ReportRunStatus> waitFor(@RequestBody final WaitforRequest request) {
		if (configService.unsecuredOperationPattern != null) {
			final Matcher matcher = configService.unsecuredOperationPattern.matcher("waitfor");
			if (!matcher.matches()) {
				final String securityToken = request.getSecurityToken();
				try {
					authorizationService.authorize(securityToken, null);
				}
				catch (final BadRequestException e) {
					return new ResponseEntity<ReportRunStatus>(e.getCode());
				}
				catch (final SQLException e) {
					return new ResponseEntity<ReportRunStatus>(HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}
		}
		UUID uuid;
		try {
			uuid = UUID.fromString(request.getJobId());
		}
		catch (final IllegalArgumentException e) {
			return new ResponseEntity<ReportRunStatus>(HttpStatus.NOT_ACCEPTABLE);
		}
		final ReportRunStatus status = runner.getStatus(uuid);
		synchronized (status) {
			final Long timeout = request.getTimeout();
			try {
				if (timeout != null) {
					status.wait(timeout.longValue());
				}
				else {
					status.wait();
				}
			}
			catch (final InterruptedException e) {
			}
		}
		return new ResponseEntity<ReportRunStatus>(status, HttpStatus.OK);
	}

	@PostMapping("/submit")
	@ResponseBody
	public ResponseEntity<SubmitResponse> submit(@RequestBody final SubmitRequest request) {
		logger.info("submit " + request);
		try {
			final UUID uuid = UUID.randomUUID();
			final String format = runner.getFormat(request.getFormat());
			final String outputFilename = uuid + "." + format;
			final ReportRun reportRun = new ReportRun(request.getDesignFile(),
					request.getNameForHumans(), format, outputFilename, request.isRunThenRender(),
					runner.fixParameterTypes(request.getParameters()), request.getSecurityToken());
			final ReportEmail email = new ReportEmail(request.isSendEmailOnSuccess(),
					request.isSendEmailOnFailure(), request.getMailTo(), request.getMailCc(),
					request.getMailBcc(), request.getMailSuccessSubject(),
					request.getMailFailureSubject(), request.getMailSuccessBody(),
					request.getMailFailureBody(), request.getMailAttachReport(),
					request.getMailHtml());
			final UUID jobUUID = runner.startReport(reportRun, email, true);
			return new ResponseEntity<SubmitResponse>(new SubmitResponse(jobUUID, null),
					HttpStatus.OK);
		}
		catch (final Throwable e) {
			logger.error("Exception", e);
			return new ResponseEntity<SubmitResponse>(new SubmitResponse(null, e),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/schedule-simple")
	@ResponseBody
	public ResponseEntity<ScheduleResponse> scheduleSimple(
			@RequestBody final SimpleScheduleRequest request) {
		logger.info("schedule-simple " + request);
		if (configService.unsecuredOperationPattern != null) {
			final Matcher matcher = configService.unsecuredOperationPattern.matcher(
				"schedule-simple");
			if (!matcher.matches()) {
				final String securityToken = request.getSecurityToken();
				try {
					authorizationService.authorize(securityToken, null);
				}
				catch (final BadRequestException e) {
					return new ResponseEntity<ScheduleResponse>(e.getCode());
				}
				catch (final SQLException e) {
					return new ResponseEntity<ScheduleResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}
		}
		try {
			final Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			if (!scheduler.isStarted()) {
				scheduler.start();
			}
			if (scheduler.isShutdown()) {
				throw new RuntimeException("Scheduler has been shut down");
			}
			final JobDetail jobDetail = newJob(RunReportQuartzJob.class).withIdentity(
				request.getName(), request.getGroup()).build();
			final JobDataMap jobDataMap = jobDetail.getJobDataMap();
			jobDataMap.put("submitRequest", request.getSubmit());
			jobDataMap.put("runnerService", runner);
			jobDataMap.put("schedulerService", schedulerService);
			final TriggerBuilder<Trigger> triggerBuilder = newTrigger().withIdentity(
				request.getName() + "-trigger", request.getGroup());
			final Date startDate = request.getStartDate();
			if (startDate == null) {
				triggerBuilder.startNow();
			}
			else {
				triggerBuilder.startAt(startDate);
			}
			final SimpleScheduleBuilder simpleScheduleBuilder = simpleSchedule();
			final Long intervalInMilliseconds = request.getIntervalInMilliseconds();
			if (intervalInMilliseconds != null) {
				simpleScheduleBuilder.withIntervalInMilliseconds(
					intervalInMilliseconds.longValue());
			}
			final Integer repeatCount = request.getRepeatCount();
			if (repeatCount != null) {
				simpleScheduleBuilder.withRepeatCount(repeatCount.intValue());
			}
			else {
				simpleScheduleBuilder.repeatForever();
			}
			final String misfireInstruction = request.getMisfireInstruction();
			if ("fire-now".equals(misfireInstruction)) {
				simpleScheduleBuilder.withMisfireHandlingInstructionFireNow();
			}
			else if ("ignore".equals(misfireInstruction)) {
				simpleScheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();
			}
			else if ("next-with-existing-count".equals(misfireInstruction)) {
				simpleScheduleBuilder.withMisfireHandlingInstructionNextWithExistingCount();
			}
			else if ("next-with-remaining-count".equals(misfireInstruction)) {
				simpleScheduleBuilder.withMisfireHandlingInstructionNextWithRemainingCount();
			}
			else if ("now-with-existing-count".equals(misfireInstruction)) {
				simpleScheduleBuilder.withMisfireHandlingInstructionNowWithExistingCount();
			}
			else if ("now-with-remaining-count".equals(misfireInstruction)) {
				simpleScheduleBuilder.withMisfireHandlingInstructionNowWithRemainingCount();
			}
			else if (misfireInstruction != null) {
				throw new RuntimeException("Unrecognized misfire instruction");
			}
			triggerBuilder.withSchedule(simpleScheduleBuilder);
			final Trigger trigger = triggerBuilder.build();
			scheduler.scheduleJob(jobDetail, trigger);
			return new ResponseEntity<ScheduleResponse>(
					new ScheduleResponse(trigger.getJobKey(), null), HttpStatus.OK);
		}
		catch (final SchedulerException e) {
			logger.error("Exception", e);
			return new ResponseEntity<ScheduleResponse>(new ScheduleResponse(null, e.getMessage()),
					HttpStatus.BAD_REQUEST);
		}
		catch (final Throwable e) {
			logger.error("Exception", e);
			return new ResponseEntity<ScheduleResponse>(new ScheduleResponse(null, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/schedule-cron")
	@ResponseBody
	public ResponseEntity<ScheduleResponse> scheduleCron(
			@RequestBody final CronScheduleRequest request) {
		logger.info("schedule-cron " + request);
		if (configService.unsecuredOperationPattern != null) {
			final Matcher matcher = configService.unsecuredOperationPattern.matcher(
				"schedule-cron");
			if (!matcher.matches()) {
				final String securityToken = request.getSecurityToken();
				try {
					authorizationService.authorize(securityToken,
						request.getSubmit().getDesignFile());
				}
				catch (final BadRequestException e) {
					return new ResponseEntity<ScheduleResponse>(e.getCode());
				}
				catch (final SQLException e) {
					return new ResponseEntity<ScheduleResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}
		}
		try {
			final Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			if (!scheduler.isStarted()) {
				scheduler.start();
			}
			if (scheduler.isShutdown()) {
				throw new RuntimeException("Scheduler has been shut down");
			}
			final JobDetail jobDetail = newJob(RunReportQuartzJob.class).withIdentity(
				request.getName(), request.getGroup()).build();
			final JobDataMap jobDataMap = jobDetail.getJobDataMap();
			jobDataMap.put("submitRequest", request.getSubmit());
			jobDataMap.put("runnerService", runner);
			jobDataMap.put("schedulerService", schedulerService);
			final TriggerBuilder<Trigger> triggerBuilder = newTrigger().withIdentity(
				request.getName() + "-trigger", request.getGroup());
			final Date startDate = request.getStartDate();
			if (startDate == null) {
				triggerBuilder.startNow();
			}
			else {
				triggerBuilder.startAt(startDate);
			}
			final CronScheduleBuilder scheduleBuilder = cronSchedule(request.getCronString());
			final String misfireInstruction = request.getMisfireInstruction();
			if ("ignore".equals(misfireInstruction)) {
				scheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();
			}
			else if ("fire-and-proceed".equals(misfireInstruction)) {
				scheduleBuilder.withMisfireHandlingInstructionFireAndProceed();
			}
			else if ("do-nothing".equals(misfireInstruction)) {
				scheduleBuilder.withMisfireHandlingInstructionDoNothing();
			}
			else if (misfireInstruction != null) {
				throw new RuntimeException("Unrecognized misfire instruction");
			}
			triggerBuilder.withSchedule(scheduleBuilder);
			final Trigger trigger = triggerBuilder.build();
			scheduler.scheduleJob(jobDetail, trigger);
			return new ResponseEntity<ScheduleResponse>(
					new ScheduleResponse(trigger.getJobKey(), null), HttpStatus.OK);
		}
		catch (final SchedulerException e) {
			logger.error("Exception", e);
			return new ResponseEntity<ScheduleResponse>(new ScheduleResponse(null, e.getMessage()),
					HttpStatus.BAD_REQUEST);
		}
		catch (final Throwable e) {
			logger.error("Exception", e);
			return new ResponseEntity<ScheduleResponse>(new ScheduleResponse(null, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/schedules")
	@ResponseBody
	public ResponseEntity<Map<JobKey, List<Trigger>>> getSchedules(
			@RequestBody final BaseRequest request) {
		logger.info("schedules " + request);
		if (configService.unsecuredOperationPattern != null) {
			final Matcher matcher = configService.unsecuredOperationPattern.matcher("schedules");
			if (!matcher.matches()) {
				final String securityToken = request.getSecurityToken();
				try {
					authorizationService.authorize(securityToken, null);
				}
				catch (final BadRequestException e) {
					return new ResponseEntity<Map<JobKey, List<Trigger>>>(e.getCode());
				}
				catch (final SQLException e) {
					return new ResponseEntity<Map<JobKey, List<Trigger>>>(
							HttpStatus.INTERNAL_SERVER_ERROR);
				}
			}
		}
		final Map<JobKey, List<Trigger>> response = new HashMap<>();
		try {
			final Scheduler scheduler = new StdSchedulerFactory().getScheduler();
			for (final JobKey jobKey : scheduler.getJobKeys(GroupMatcher.anyJobGroup())) {
				@SuppressWarnings("unchecked")
				final List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
				response.put(jobKey, triggers);
			}
		}
		catch (final SchedulerException e) {
			logger.error("Exception", e);
			return new ResponseEntity<Map<JobKey, List<Trigger>>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<Map<JobKey, List<Trigger>>>(response, HttpStatus.OK);
	}

	@PostMapping("/run")
	@ResponseBody
	public ResponseEntity<Resource> run(@RequestBody final RunRequest request) {
		logger.info("run");
		try {
			final String format = runner.getFormat(request.getFormat());
			final String outputFilename = UUID.randomUUID() + "." + format;
			final ReportRun reportRun = new ReportRun(request.getDesignFile(), null, format,
					outputFilename, request.isRunThenRender(),
					runner.fixParameterTypes(request.getParameters()), request.getSecurityToken());
			final List<Exception> exceptions = runner.runReport(reportRun);
			if (!exceptions.isEmpty()) {
				for (final Throwable e : exceptions) {
					logger.error("Exception", e);
				}
				return new ResponseEntity<Resource>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
			final File outputDir = runner.getOutputDirectory();
			final File outputFile = outputDir == null ? new File(outputFilename)
				: new File(outputDir, outputFilename);
			final InputStream inputStream = new FileInputStream(outputFile);
			final HttpHeaders headers = new HttpHeaders();
			// headers.set("Content-Disposition", "attachment; filename=\"" + outputFilename + "\"");
			final InputStreamResource resource = new InputStreamResource(inputStream);
			return ResponseEntity.ok().headers(headers).contentLength(
				outputFile.length()).contentType(getMediaType(format)).body(resource);
		}
		catch (final BadRequestException e) {
			return getErrorResponse(e.getCode(), e.getReason());
		}
		catch (final Throwable e) {
			logger.error("Exception", e);
			return getErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.toString());
		}
	}

	private MediaType getMediaType(final String format) {
		if ("pdf".equalsIgnoreCase(format)) {
			return MediaType.APPLICATION_PDF;
		}
		if ("html".equalsIgnoreCase(format)) {
			return MediaType.TEXT_HTML;
		}
		if ("xls".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/vnd.ms-excel");
		}
		if ("xlsx".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType(
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		}
		if ("doc".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/ms-word");
		}
		if ("docx".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType(
				"application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		}
		if ("ppt".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/vnd.ms-powerpoint");
		}
		if ("pptx".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType(
				"application/vnd.openxmlformats-officedocument.presentationml.presentation");
		}
		if (".odp".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/vnd.oasis.opendocument.presentation");
		}
		if (".ods".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/vnd.oasis.opendocument.spreadsheet");
		}
		if (".odt".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/vnd.oasis.opendocument.text");
		}
		return MediaType.APPLICATION_OCTET_STREAM;
	}

	public static ResponseEntity<Resource> getErrorResponse(final HttpStatus code,
			final String reason) {
		final HttpHeaders headers = new HttpHeaders();
		final ByteArrayResource resource = new ByteArrayResource(reason.getBytes());
		return ResponseEntity.status(code).headers(headers).contentType(MediaType.TEXT_PLAIN).body(
			resource);
	}
}
