/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.brr.controller;

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

import javax.annotation.PreDestroy;

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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.innoventsolutions.brr.ReportRun;
import com.innoventsolutions.brr.ReportRunStatus;
import com.innoventsolutions.brr.entity.BaseRequest;
import com.innoventsolutions.brr.entity.CronScheduleRequest;
import com.innoventsolutions.brr.entity.GetJobRequest;
import com.innoventsolutions.brr.entity.ReportEmail;
import com.innoventsolutions.brr.entity.RunRequest;
import com.innoventsolutions.brr.entity.ScheduleResponse;
import com.innoventsolutions.brr.entity.SimpleScheduleRequest;
import com.innoventsolutions.brr.entity.StatusRequest;
import com.innoventsolutions.brr.entity.SubmitRequest;
import com.innoventsolutions.brr.entity.SubmitResponse;
import com.innoventsolutions.brr.entity.WaitforRequest;
import com.innoventsolutions.brr.exception.BadRequestException;
import com.innoventsolutions.brr.jobs.RunReportQuartzJob;
import com.innoventsolutions.brr.service.AuthorizationService;
import com.innoventsolutions.brr.service.ConfigService;
import com.innoventsolutions.brr.service.RunnerService;
import com.innoventsolutions.brr.service.SchedulerService;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Controller
public class JobController {
	private RunnerService runner;
	private SchedulerService schedulerService;
	private ConfigService configService;
	private AuthorizationService authorizationService;

	@GetMapping("/welcome")
	public String loginMessage(final Model model) {
		model.addAttribute("message", "test");
		return "welcome"; // view
	}

	@Autowired
	public JobController(RunnerService runner, final ConfigService configService,
			final AuthorizationService authorizationService, final SchedulerService schedulerService) {
		this.runner = runner;
		this.configService = configService;
		this.authorizationService = authorizationService;
		this.schedulerService = schedulerService;
	}

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

	public ResponseEntity<Resource> getReport(final StatusRequest request, final boolean isAttachment) {
		log.info("getReport " + request + " " + isAttachment);
		if (!allow(isAttachment ? "download" : "get")) {
			final String securityToken = request.getSecurityToken();
			try {
				authorizationService.authorize(securityToken, null);
			} catch (final BadRequestException e) {
				return new ResponseEntity<Resource>(e.getCode());
			} catch (final SQLException e) {
				return new ResponseEntity<Resource>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		try {
			final File outputDir = runner.getOutputDirectory();
			log.info("outputDir = " + outputDir);
			UUID uuid;
			try {
				uuid = UUID.fromString(request.getJobId());
			} catch (final IllegalArgumentException e) {
				return getErrorResponse(HttpStatus.NOT_ACCEPTABLE, "Invalid or missing UUID");
			}
			final ReportRunStatus status = runner.getStatus(uuid);
			if (status == null) {
				return getErrorResponse(HttpStatus.NOT_FOUND, "Report not found");
			}
			while (!status.isFinished()) {
				log.info("waiting...");
				synchronized (status) {
					try {
						status.wait();
					} catch (final InterruptedException e) {
					}
				}
				log.info("done waiting");
			}
			if (!status.isFinished()) {
				return getErrorResponse(HttpStatus.BAD_REQUEST, "Report is not finished");
			}
			final File outputFile = outputDir == null ? new File(status.reportRun.outputFile)
					: new File(outputDir, status.reportRun.outputFile);
			final InputStream inputStream = new FileInputStream(outputFile);
			final HttpHeaders headers = new HttpHeaders();
			if (isAttachment) {
				headers.set("Content-Disposition", "attachment; filename=\"" + status.reportRun.outputFile + "\"");
			}
			final InputStreamResource resource = new InputStreamResource(inputStream);
			return ResponseEntity.ok().headers(headers).contentLength(outputFile.length())
					.contentType(getMediaType(status.reportRun.format)).body(resource);
		} catch (final Throwable e) {
			log.error("Exception", e);
			return getErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.toString());
		}
	}

	@GetMapping("/status")
	@ResponseBody
	public ResponseEntity<ReportRunStatus> getStatus(@RequestBody final StatusRequest request) {
		log.info("getStatus " + request);
		if (!allow("status")) {
			final String securityToken = request.getSecurityToken();
			try {
				authorizationService.authorize(securityToken, null);
			} catch (final BadRequestException e) {
				return new ResponseEntity<ReportRunStatus>(e.getCode());
			} catch (final SQLException e) {
				return new ResponseEntity<ReportRunStatus>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		UUID uuid;
		try {
			uuid = UUID.fromString(request.getJobId());
		} catch (final IllegalArgumentException e) {
			return new ResponseEntity<ReportRunStatus>(HttpStatus.NOT_ACCEPTABLE);
		}
		final ReportRunStatus status = runner.getStatus(uuid);
		return new ResponseEntity<ReportRunStatus>(status, HttpStatus.OK);
	}

	@GetMapping("/test")
	@ResponseBody
	public ResponseEntity<Map<UUID, ReportRunStatus>> getTest(@RequestBody final BaseRequest request) {
		log.info("getTest " + request);
		if (!allow("status-all")) {
			final String securityToken = request.getSecurityToken();
			try {
				authorizationService.authorize(securityToken, null);
			} catch (final BadRequestException e) {
				return new ResponseEntity<Map<UUID, ReportRunStatus>>(e.getCode());
			} catch (final SQLException e) {
				return new ResponseEntity<Map<UUID, ReportRunStatus>>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		final Map<UUID, ReportRunStatus> status = runner.getStatusAll();
		return new ResponseEntity<Map<UUID, ReportRunStatus>>(status, HttpStatus.OK);
	}

	@GetMapping("/status-all")
	@ResponseBody
	public ResponseEntity<Map<UUID, ReportRunStatus>> getStatusAll(@RequestBody final BaseRequest request) {
		log.info("getStatusAll " + request);
		if (!allow("status-all")) {
			final String securityToken = request.getSecurityToken();
			try {
				authorizationService.authorize(securityToken, null);
			} catch (final BadRequestException e) {
				return new ResponseEntity<Map<UUID, ReportRunStatus>>(e.getCode());
			} catch (final SQLException e) {
				return new ResponseEntity<Map<UUID, ReportRunStatus>>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		final Map<UUID, ReportRunStatus> status = runner.getStatusAll();
		return new ResponseEntity<Map<UUID, ReportRunStatus>>(status, HttpStatus.OK);
	}

	@GetMapping("/waitfor")
	@ResponseBody
	public ResponseEntity<ReportRunStatus> waitFor(@RequestBody final WaitforRequest request) {
		log.info("waitFor " + request);
		if (!allow("waitfor")) {
			final String securityToken = request.getSecurityToken();
			try {
				authorizationService.authorize(securityToken, null);
			} catch (final BadRequestException e) {
				return new ResponseEntity<ReportRunStatus>(e.getCode());
			} catch (final SQLException e) {
				return new ResponseEntity<ReportRunStatus>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		UUID uuid;
		try {
			uuid = UUID.fromString(request.getJobId());
		} catch (final IllegalArgumentException e) {
			return new ResponseEntity<ReportRunStatus>(HttpStatus.NOT_ACCEPTABLE);
		}
		final ReportRunStatus status = runner.getStatus(uuid);
		synchronized (status) {
			final Long timeout = request.getTimeout();
			try {
				if (timeout != null) {
					status.wait(timeout.longValue());
				} else {
					status.wait();
				}
			} catch (final InterruptedException e) {
			}
		}
		return new ResponseEntity<ReportRunStatus>(status, HttpStatus.OK);
	}

	@PostMapping("/submit")
	@ResponseBody
	public ResponseEntity<SubmitResponse> submit(@RequestBody final SubmitRequest request) {
		log.info("submit " + request);
		try {
			final UUID uuid = UUID.randomUUID();
			final String format = runner.getFormat(request.getFormat());
			final String outputFilename = uuid + "." + format;
			final ReportRun reportRun = new ReportRun(request.getDesignFile(), request.getNameForHumans(), format,
					outputFilename, request.isRunThenRender(), runner.fixParameterTypes(request.getParameters()),
					request.getSecurityToken());
			final ReportEmail email = new ReportEmail(request.getSendEmailOnSuccess(), request.getSendEmailOnFailure(),
					request.getMailTo(), request.getMailCc(), request.getMailBcc(), request.getMailSuccessSubject(),
					request.getMailFailureSubject(), request.getMailSuccessBody(), request.getMailFailureBody(),
					request.getMailAttachReport(), request.getMailHtml());
			final UUID jobUUID = runner.startReport(reportRun, email, true);
			return new ResponseEntity<SubmitResponse>(new SubmitResponse(jobUUID, null), HttpStatus.OK);
		} catch (final BadRequestException e) {
			log.error("Exception", e);
			return new ResponseEntity<SubmitResponse>(new SubmitResponse(null, e), e.getCode());
		} catch (final Throwable e) {
			log.error("Exception", e);
			return new ResponseEntity<SubmitResponse>(new SubmitResponse(null, e), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/schedule-simple")
	@ResponseBody
	public ResponseEntity<ScheduleResponse> scheduleSimple(@RequestBody final SimpleScheduleRequest request) {
		log.info("schedule-simple " + request);
		final String designFileName = request.getSubmit().getDesignFile();
		if (!allowReport(designFileName)) {
			final String securityToken = request.getSecurityToken();
			try {
				authorizationService.authorize(securityToken, designFileName);
			} catch (final BadRequestException e) {
				return new ResponseEntity<ScheduleResponse>(e.getCode());
			} catch (final SQLException e) {
				return new ResponseEntity<ScheduleResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
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
			final JobDetail jobDetail = newJob(RunReportQuartzJob.class)
					.withIdentity(request.getName(), request.getGroup()).build();
			final JobDataMap jobDataMap = jobDetail.getJobDataMap();
			jobDataMap.put("submitRequest", request.getSubmit());
			jobDataMap.put("runnerService", runner);
			jobDataMap.put("schedulerService", schedulerService);
			final TriggerBuilder<Trigger> triggerBuilder = newTrigger().withIdentity(request.getName() + "-trigger",
					request.getGroup());
			final Date startDate = request.getStartDate();
			if (startDate == null) {
				triggerBuilder.startNow();
			} else {
				triggerBuilder.startAt(startDate);
			}
			final SimpleScheduleBuilder simpleScheduleBuilder = simpleSchedule();
			final Long intervalInMilliseconds = request.getIntervalInMilliseconds();
			if (intervalInMilliseconds != null) {
				simpleScheduleBuilder.withIntervalInMilliseconds(intervalInMilliseconds.longValue());
			}
			final Integer repeatCount = request.getRepeatCount();
			if (repeatCount != null) {
				simpleScheduleBuilder.withRepeatCount(repeatCount.intValue());
			} else {
				simpleScheduleBuilder.repeatForever();
			}
			final String misfireInstruction = request.getMisfireInstruction();
			if ("fire-now".equals(misfireInstruction)) {
				simpleScheduleBuilder.withMisfireHandlingInstructionFireNow();
			} else if ("ignore".equals(misfireInstruction)) {
				simpleScheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();
			} else if ("next-with-existing-count".equals(misfireInstruction)) {
				simpleScheduleBuilder.withMisfireHandlingInstructionNextWithExistingCount();
			} else if ("next-with-remaining-count".equals(misfireInstruction)) {
				simpleScheduleBuilder.withMisfireHandlingInstructionNextWithRemainingCount();
			} else if ("now-with-existing-count".equals(misfireInstruction)) {
				simpleScheduleBuilder.withMisfireHandlingInstructionNowWithExistingCount();
			} else if ("now-with-remaining-count".equals(misfireInstruction)) {
				simpleScheduleBuilder.withMisfireHandlingInstructionNowWithRemainingCount();
			} else if (misfireInstruction != null) {
				throw new RuntimeException("Unrecognized misfire instruction");
			}
			triggerBuilder.withSchedule(simpleScheduleBuilder);
			final Trigger trigger = triggerBuilder.build();
			scheduler.scheduleJob(jobDetail, trigger);
			return new ResponseEntity<ScheduleResponse>(new ScheduleResponse(trigger.getJobKey(), null), HttpStatus.OK);
		} catch (final SchedulerException e) {
			log.error("Exception", e);
			return new ResponseEntity<ScheduleResponse>(new ScheduleResponse(null, e.getMessage()),
					HttpStatus.BAD_REQUEST);
		} catch (final Throwable e) {
			log.error("Exception", e);
			return new ResponseEntity<ScheduleResponse>(new ScheduleResponse(null, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/schedule-cron")
	@ResponseBody
	public ResponseEntity<ScheduleResponse> scheduleCron(@RequestBody final CronScheduleRequest request) {
		log.info("schedule-cron " + request);
		final String designFileName = request.getSubmit().getDesignFile();
		if (!allowReport(designFileName)) {
			final String securityToken = request.getSecurityToken();
			try {
				authorizationService.authorize(securityToken, designFileName);
			} catch (final BadRequestException e) {
				return new ResponseEntity<ScheduleResponse>(e.getCode());
			} catch (final SQLException e) {
				return new ResponseEntity<ScheduleResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
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
			final JobDetail jobDetail = newJob(RunReportQuartzJob.class)
					.withIdentity(request.getName(), request.getGroup()).build();
			final JobDataMap jobDataMap = jobDetail.getJobDataMap();
			jobDataMap.put("submitRequest", request.getSubmit());
			jobDataMap.put("runnerService", runner);
			jobDataMap.put("schedulerService", schedulerService);
			final TriggerBuilder<Trigger> triggerBuilder = newTrigger().withIdentity(request.getName() + "-trigger",
					request.getGroup());
			final Date startDate = request.getStartDate();
			log.info("startDate = " + startDate);
			if (startDate == null) {
				triggerBuilder.startNow();
			} else {
				triggerBuilder.startAt(startDate);
			}
			final String cronString = request.getCronString();
			log.info("cronString = " + cronString);
			final CronScheduleBuilder scheduleBuilder = cronSchedule(cronString);
			final String misfireInstruction = request.getMisfireInstruction();
			log.info("misfireInstruction = " + misfireInstruction);
			if ("ignore".equals(misfireInstruction)) {
				scheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();
			} else if ("fire-and-proceed".equals(misfireInstruction)) {
				scheduleBuilder.withMisfireHandlingInstructionFireAndProceed();
			} else if ("do-nothing".equals(misfireInstruction)) {
				scheduleBuilder.withMisfireHandlingInstructionDoNothing();
			} else if (misfireInstruction != null) {
				throw new RuntimeException("Unrecognized misfire instruction");
			}
			triggerBuilder.withSchedule(scheduleBuilder);
			final Trigger trigger = triggerBuilder.build();
			scheduler.scheduleJob(jobDetail, trigger);
			return new ResponseEntity<ScheduleResponse>(new ScheduleResponse(trigger.getJobKey(), null), HttpStatus.OK);
		} catch (final SchedulerException e) {
			log.error("Exception", e);
			return new ResponseEntity<ScheduleResponse>(new ScheduleResponse(null, e.getMessage()),
					HttpStatus.BAD_REQUEST);
		} catch (final Throwable e) {
			log.error("Exception", e);
			return new ResponseEntity<ScheduleResponse>(new ScheduleResponse(null, e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public static class DeleteJobResponse {
		private final boolean jobDeleted;

		public DeleteJobResponse(final boolean jobDeleted) {
			this.jobDeleted = jobDeleted;
		}

		public boolean isJobDeleted() {
			return jobDeleted;
		}
	}

	@DeleteMapping("/job")
	@ResponseBody
	public ResponseEntity<DeleteJobResponse> deleteJob(@RequestBody final GetJobRequest request) {
		log.info("delete-job " + request);
		if (!allow("delete-job")) {
			final String securityToken = request.getSecurityToken();
			try {
				authorizationService.authorize(securityToken, null);
			} catch (final BadRequestException e) {
				return new ResponseEntity<DeleteJobResponse>(e.getCode());
			} catch (final SQLException e) {
				return new ResponseEntity<DeleteJobResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		try {
			final Scheduler scheduler = new StdSchedulerFactory().getScheduler();
			final JobKey jobKey = new JobKey(request.getName(), request.getGroup());
			final boolean result = scheduler.deleteJob(jobKey);
			return new ResponseEntity<DeleteJobResponse>(new DeleteJobResponse(result), HttpStatus.OK);
		} catch (final SchedulerException e) {
			log.error("Exception", e);
			return new ResponseEntity<DeleteJobResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	static class JobResponse {
		List<Trigger> triggers;
		Map<UUID, ReportRunStatus> runs;

		public List<Trigger> getTriggers() {
			return triggers;
		}

		public void setTriggers(final List<Trigger> triggers) {
			this.triggers = triggers;
		}

		public Map<UUID, ReportRunStatus> getRuns() {
			return runs;
		}

		public void setRuns(final Map<UUID, ReportRunStatus> runs) {
			this.runs = runs;
		}
	}

	@GetMapping("/job")
	@ResponseBody
	public ResponseEntity<JobResponse> getJob(@RequestBody final GetJobRequest request) {
		log.info("job " + request);
		if (!allow("job")) {
			final String securityToken = request.getSecurityToken();
			try {
				authorizationService.authorize(securityToken, null);
			} catch (final BadRequestException e) {
				return new ResponseEntity<JobResponse>(e.getCode());
			} catch (final SQLException e) {
				return new ResponseEntity<JobResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		try {
			final Scheduler scheduler = new StdSchedulerFactory().getScheduler();
			final JobKey jobKey = new JobKey(request.getName(), request.getGroup());
			final JobResponse jobResponse = new JobResponse();
			@SuppressWarnings("unchecked")
			final List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
			jobResponse.triggers = triggers;
			final List<UUID> uuids = schedulerService.getJob(jobKey);
			jobResponse.runs = new HashMap<>();
			if (uuids != null) {
				for (final UUID uuid : uuids) {
					jobResponse.runs.put(uuid, runner.getStatusAll().get(uuid));
				}
			}
			return new ResponseEntity<JobResponse>(jobResponse, HttpStatus.OK);
		} catch (final SchedulerException e) {
			log.error("Exception", e);
			return new ResponseEntity<JobResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/jobs")
	@ResponseBody
	public ResponseEntity<Map<JobKey, JobResponse>> getJobs(@RequestBody final BaseRequest request) {
		log.info("jobs " + request);
		if (!allow("jobs")) {
			final String securityToken = request.getSecurityToken();
			try {
				authorizationService.authorize(securityToken, null);
			} catch (final BadRequestException e) {
				return new ResponseEntity<Map<JobKey, JobResponse>>(e.getCode());
			} catch (final SQLException e) {
				return new ResponseEntity<Map<JobKey, JobResponse>>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		final Map<JobKey, JobResponse> response = new HashMap<>();
		try {
			final Scheduler scheduler = new StdSchedulerFactory().getScheduler();
			for (final JobKey jobKey : scheduler.getJobKeys(GroupMatcher.anyJobGroup())) {
				final JobResponse jobResponse = new JobResponse();
				@SuppressWarnings("unchecked")
				final List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
				jobResponse.triggers = triggers;
				final List<UUID> uuids = schedulerService.getJob(jobKey);
				jobResponse.runs = new HashMap<>();
				if (uuids != null) {
					for (final UUID uuid : uuids) {
						jobResponse.runs.put(uuid, runner.getStatusAll().get(uuid));
					}
				}
				response.put(jobKey, jobResponse);
			}
		} catch (final SchedulerException e) {
			log.error("Exception", e);
			return new ResponseEntity<Map<JobKey, JobResponse>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<Map<JobKey, JobResponse>>(response, HttpStatus.OK);
	}

	@PostMapping("/run")
	@ResponseBody
	public ResponseEntity<Resource> run(@RequestBody final RunRequest request) {
		log.info("run");
		try {
			final String format = runner.getFormat(request.getFormat());
			final String outputFilename = UUID.randomUUID() + "." + format;
			final ReportRun reportRun = new ReportRun(request.getDesignFile(), null, format, outputFilename,
					request.isRunThenRender(), runner.fixParameterTypes(request.getParameters()),
					request.getSecurityToken());
			final List<Exception> exceptions = runner.runReport(reportRun);
			if (!exceptions.isEmpty()) {
				for (final Throwable e : exceptions) {
					log.error("Exception", e);
				}
				return new ResponseEntity<Resource>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
			final File outputDir = runner.getOutputDirectory();
			final File outputFile = outputDir == null ? new File(outputFilename) : new File(outputDir, outputFilename);
			final InputStream inputStream = new FileInputStream(outputFile);
			final HttpHeaders headers = new HttpHeaders();
			// headers.set("Content-Disposition", "attachment; filename=\"" + outputFilename + "\"");
			final InputStreamResource resource = new InputStreamResource(inputStream);
			return ResponseEntity.ok().headers(headers).contentLength(outputFile.length())
					.contentType(getMediaType(format)).body(resource);
		} catch (final BadRequestException e) {
			return getErrorResponse(e.getCode(), e.getReason());
		} catch (final Throwable e) {
			log.error("Exception", e);
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
			return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		}
		if ("doc".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/ms-word");
		}
		if ("docx".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		}
		if ("ppt".equalsIgnoreCase(format)) {
			return MediaType.parseMediaType("application/vnd.ms-powerpoint");
		}
		if ("pptx".equalsIgnoreCase(format)) {
			return MediaType
					.parseMediaType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
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

	public static ResponseEntity<Resource> getErrorResponse(final HttpStatus code, final String reason) {
		final HttpHeaders headers = new HttpHeaders();
		final ByteArrayResource resource = new ByteArrayResource(reason.getBytes());
		return ResponseEntity.status(code).headers(headers).contentType(MediaType.TEXT_PLAIN).body(resource);
	}

	@PreDestroy
	public void onExit() {
		log.info("onExit");
		try {
			final Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			if (!scheduler.isShutdown()) {
				scheduler.shutdown(true);
			}
		} catch (final SchedulerException e) {
			log.error("Unable to acquire scheduler", e);
		}
	}

	private boolean allow(final String opName) {
		if (configService.unsecuredOperationPattern != null) {
			final Matcher matcher = configService.unsecuredOperationPattern.matcher(opName);
			return matcher.matches();
		}
		return false;
	}

	private boolean allowReport(final String designFileName) {
		if (configService.unsecuredDesignFilePattern != null) {
			final Matcher matcher = configService.unsecuredDesignFilePattern.matcher(designFileName);
			return matcher.matches();
		}
		return false;
	}
}
