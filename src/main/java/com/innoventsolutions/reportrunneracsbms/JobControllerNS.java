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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.innoventsolutions.birt.runner.BadRequestException;
import com.innoventsolutions.birt.runner.ReportEmail;
import com.innoventsolutions.birt.runner.ReportRun;
import com.innoventsolutions.birt.runner.ReportRunStatus;
import com.innoventsolutions.birt.runner.RunnerContext;

@Controller
@RequestMapping("/nonspring")
public class JobControllerNS {
	Logger logger = LoggerFactory.getLogger(JobControllerNS.class);

	@GetMapping("/welcome")
	public String loginMessage() {
		return "welcome";
	}

	@GetMapping("/get/{uuid}")
	@ResponseBody
	public ResponseEntity<Resource> getReport(@PathVariable("uuid") final String uuidString) {
		return getReport(uuidString, false);
	}

	@GetMapping("/download/{uuid}")
	@ResponseBody
	public ResponseEntity<Resource> downloadReport(@PathVariable("uuid") final String uuidString) {
		return getReport(uuidString, true);
	}

	public ResponseEntity<Resource> getReport(final String uuidString, final boolean isAttachment) {
		try {
			logger.info("getReport " + uuidString + " " + isAttachment);
			final File outputDir = runnerContext.getOutputDirectory();
			logger.info("outputDir = " + outputDir);
			UUID uuid;
			try {
				uuid = UUID.fromString(uuidString);
			}
			catch (final IllegalArgumentException e) {
				return getErrorResponse(HttpStatus.NOT_ACCEPTABLE, "Invalid or missing UUID");
			}
			final ReportRunStatus status = runnerContext.getStatus(uuid);
			if (status == null) {
				return getErrorResponse(HttpStatus.NOT_FOUND, "Report not found");
			}
			logger.info("waiting...");
			synchronized (status) {
				status.wait();
			}
			logger.info("done waiting");
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

	@GetMapping("/status/{uuid}")
	@ResponseBody
	public ReportRunStatus getStatus(@PathVariable("uuid") final String uuid) {
		return runnerContext.getStatus(UUID.fromString(uuid));
	}

	@GetMapping("/waitfor/{uuid}")
	@ResponseBody
	public ReportRunStatus waitFor(@PathVariable("uuid") final String uuid) {
		final ReportRunStatus status = runnerContext.getStatus(UUID.fromString(uuid));
		synchronized (status) {
			try {
				status.wait();
			}
			catch (final InterruptedException e) {
			}
		}
		return status;
	}

	@PostMapping("/submit")
	@ResponseBody
	public SubmitResponse submit(@RequestBody final SubmitRequest request) {
		logger.info("submit " + request);
		if (runnerContext == null) {
			logger.error("runnerContext is null - " + initException);
			return new SubmitResponse(null, initException);
		}
		try {
			final UUID uuid = UUID.randomUUID();
			final String format = runnerContext.getFormat(request.getFormat());
			final String outputFilename = uuid + "." + format;
			final ReportRun reportRun = new ReportRun(request.getDesignFile(),
					request.getNameForHumans(), format, outputFilename, request.isRunThenRender(),
					fixParameterTypes(request.getParameters()), null);
			final ReportEmail email = new ReportEmail(request.isSendEmailOnSuccess(),
					request.isSendEmailOnFailure(), request.getMailTo(), request.getMailCc(),
					request.getMailBcc(), request.getMailSuccessSubject(),
					request.getMailFailureSubject(), request.getMailSuccessBody(),
					request.getMailFailureBody(), request.getMailAttachReport(),
					request.getMailHtml());
			final UUID jobUUID = runnerContext.startReport(reportRun, email);
			return new SubmitResponse(jobUUID, null);
		}
		catch (final Throwable e) {
			logger.error("Exception", e);
			return new SubmitResponse(null, e);
		}
	}

	@PostMapping("/run")
	@ResponseBody
	public ResponseEntity<Resource> run(@RequestBody final RunRequest request) {
		logger.info("run");
		if (runnerContext == null) {
			logger.error("runnerContext may not be null - " + initException);
			return getErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Server configuration error");
		}
		try {
			final String format = runnerContext.getFormat(request.getFormat());
			final String outputFilename = UUID.randomUUID() + "." + format;
			final ReportRun reportRun = new ReportRun(request.getDesignFile(), null, format,
					outputFilename, request.isRunThenRender(),
					fixParameterTypes(request.getParameters()), null);
			final List<Exception> exceptions = runnerContext.runReport(reportRun);
			if (!exceptions.isEmpty()) {
				for (final Throwable e : exceptions) {
					logger.error("Exception", e);
				}
				return new ResponseEntity<Resource>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
			final File outputDir = runnerContext.getOutputDirectory();
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

	private Map<String, Object> fixParameterTypes(final Map<String, Object> parameters)
			throws BadRequestException {
		if (parameters == null) {
			return null;
		}
		final Map<String, Object> fixedParameters = new HashMap<>();
		for (final String paramName : parameters.keySet()) {
			Object paramValue = parameters.get(paramName);
			if (paramValue instanceof Object[]) {
				final Object[] valueArray = (Object[]) paramValue;
				for (int i = 0; i < valueArray.length; i++) {
					valueArray[i] = fixParameterType(paramName, valueArray[i]);
				}
			}
			paramValue = fixParameterType(paramName, paramValue);
			fixedParameters.put(paramName, paramValue);
		}
		return fixedParameters;
	}

	private Object fixParameterType(final Object name, final Object value)
			throws BadRequestException {
		if (!(value instanceof Map)) {
			return value;
		}
		final Map<?, ?> map = (Map<?, ?>) value;
		final Object type = map.get("type");
		if (type == null) {
			logger.error("parameter value type is missing");
			throw new BadRequestException(HttpStatus.NOT_ACCEPTABLE,
					"Parameter " + name + " is an object but the type field is missing");
		}
		final Object subValue = map.get("value");
		if (!(subValue instanceof String)) {
			logger.error("parameter sub-value is not a string");
			throw new BadRequestException(HttpStatus.NOT_ACCEPTABLE, "Parameter " + name
				+ " is an object but the value field is missing or isn't a string");
		}
		if ("date".equals(type)) {
			final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			try {
				final java.util.Date date = df.parse((String) subValue);
				return new java.sql.Date(date.getTime());
			}
			catch (final ParseException e) {
				logger.error("parameter date sub-value is malformed");
				throw new BadRequestException(HttpStatus.NOT_ACCEPTABLE, "Parameter " + name
					+ " is an object and the type is date but the value isn't a valid date");
			}
		}
		if ("datetime".equals(type)) {
			final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				final java.util.Date date = df.parse((String) subValue);
				return new java.sql.Date(date.getTime());
			}
			catch (final ParseException e) {
				logger.error("parameter date sub-value is malformed");
				throw new BadRequestException(HttpStatus.NOT_ACCEPTABLE, "Parameter " + name
					+ " is an object and the type is datetime but the value isn't a valid datetime");
			}
		}
		if ("time".equals(type)) {
			final DateFormat df = new SimpleDateFormat("HH:mm:ss");
			try {
				final java.util.Date date = df.parse((String) subValue);
				return new java.sql.Time(date.getTime());
			}
			catch (final ParseException e) {
				logger.error("parameter date sub-value is malformed");
				throw new BadRequestException(HttpStatus.NOT_ACCEPTABLE, "Parameter " + name
					+ " is an object and the type is time but the value isn't a valid time");
			}
		}
		logger.error("unrecognized parameter value type: " + type);
		throw new BadRequestException(HttpStatus.NOT_ACCEPTABLE, "Parameter " + name
			+ " is an object and the type field is present but is not recognized");
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

	private static RunnerContext runnerContext = null;
	private static Exception initException = null;
	static {
		final String envName = "REPORT_RUNNER_PROPERTIES";
		final String filename = System.getenv(envName);
		if (filename == null) {
			final Logger logger = LoggerFactory.getLogger(JobControllerNS.class);
			final String msg = "Environment variable " + envName + " is missing";
			logger.error(msg);
		}
		else {
			initializeRunnerContext(filename);
		}
	}

	public static void initializeRunnerContextFromResource(final URL propertiesURL) {
		if (propertiesURL == null) {
			throw new NullPointerException("report-runner.properties not found in classpath");
		}
		final String propertiesFilename = propertiesURL.getPath();
		final Logger logger = LoggerFactory.getLogger(JobControllerNS.class);
		logger.info("propertiesFilename from resource URL = " + propertiesFilename);
		initializeRunnerContext(propertiesFilename);
	}

	public static void initializeRunnerContext(final String filename) {
		final Logger logger = LoggerFactory.getLogger(JobControllerNS.class);
		logger.info("setPropertiesFilename - JobController class = "
			+ JobControllerNS.class.getClassLoader());
		try {
			runnerContext = new RunnerContext(filename);
		}
		catch (final Exception e) {
			initException = e;
		}
	}

	public static ResponseEntity<Resource> getErrorResponse(final HttpStatus code,
			final String reason) {
		final HttpHeaders headers = new HttpHeaders();
		final ByteArrayResource resource = new ByteArrayResource(reason.getBytes());
		return ResponseEntity.status(code).headers(headers).contentType(MediaType.TEXT_PLAIN).body(
			resource);
	}
}
