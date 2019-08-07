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
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.HTMLActionHandler;
import org.eclipse.birt.report.engine.api.HTMLCompleteImageHandler;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IEngineTask;
import org.eclipse.birt.report.engine.api.IGetParameterDefinitionTask;
import org.eclipse.birt.report.engine.api.IParameterDefnBase;
import org.eclipse.birt.report.engine.api.IRenderTask;
import org.eclipse.birt.report.engine.api.IReportDocument;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.IRunTask;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.eclipse.birt.report.engine.api.UnsupportedFormatException;
import org.eclipse.birt.report.model.api.ParameterHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.innoventsolutions.birt.runner.BadRequestException;
import com.innoventsolutions.birt.runner.ReportEmail;
import com.innoventsolutions.birt.runner.ReportRun;
import com.innoventsolutions.birt.runner.ReportRunStatus;

@Service
public class RunnerService {
	private static final Logger logger = LoggerFactory.getLogger(RunnerService.class);
	private static final SimpleDateFormat PARAM_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private final ConfigService configService;
	private final BirtService birtService;
	private final MailerService mailerService;
	private final AuthorizationService authorizationService;
	public final IReportEngine engine;
	public final ExecutorService threadPool;
	public final Map<UUID, ReportRunStatus> reports = new HashMap<>();

	@Autowired
	public RunnerService(final ConfigService configService, final BirtService birtService,
			final MailerService mailerService, final AuthorizationService authorizationService)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			IOException, BirtException {
		this.configService = configService;
		this.birtService = birtService;
		this.mailerService = mailerService;
		this.authorizationService = authorizationService;
		this.threadPool = Executors.newFixedThreadPool(configService.threadCount);
		this.engine = birtService.getReportEngine();
	}

	public UUID startReport(final ReportRun reportRun, final ReportEmail email,
			final boolean authorize) throws BadRequestException, SQLException {
		logger.debug("startReport reportRun = " + reportRun + ", email = " + email);
		if (authorize) {
			authorize(reportRun);
		}
		final ReportRunStatus status = new ReportRunStatus(reportRun, email);
		UUID uuid = null; // job identifier
		synchronized (reports) {
			uuid = UUID.randomUUID();
			while (reports.containsKey(uuid)) {
				uuid = UUID.randomUUID();
			}
			// TODO clean old reports from the map
			reports.put(uuid, status);
		}
		final ReportRunnable runnable = new ReportRunnable(status, mailerService);
		threadPool.execute(runnable); // this could block
		return uuid;
	}

	class ReportRunnable implements Runnable {
		private final ReportRunStatus status;
		private final MailerService mailer;

		public ReportRunnable(final ReportRunStatus status, final MailerService mailer) {
			this.status = status;
			this.mailer = mailer;
		}

		@Override
		public void run() {
			try {
				final List<Exception> errors = runReport(status.reportRun, false);
				logger.info("report is finished, errors = " + errors);
				status.finishReport(errors);
				mailer.send(status);
			}
			catch (final Exception e) {
				final List<Exception> errors = new ArrayList<>();
				errors.add(e);
				logger.info("report failed", e);
				status.finishReport(errors);
				mailer.send(status);
			}
		}
	}

	public List<Exception> runReport(final ReportRun reportRun)
			throws EngineException, IOException, BadRequestException, SQLException {
		return runReport(reportRun, true);
	}

	@SuppressWarnings("unchecked")
	private List<Exception> runReport(final ReportRun reportRun, final boolean authorize)
			throws EngineException, IOException, BadRequestException, SQLException {
		logger.info("runReport reportRun = " + reportRun);
		if (authorize) {
			authorize(reportRun);
		}
		IReportRunnable design;
		try {
			final File designFile = getDesignFile(reportRun);
			final FileInputStream fis = new FileInputStream(designFile);
			design = engine.openReportDesign(fis);
		}
		catch (final FileNotFoundException e) {
			throw new BadRequestException(HttpStatus.NOT_FOUND, "Design file not found");
		}
		final IGetParameterDefinitionTask pdTask = engine.createGetParameterDefinitionTask(design);
		final IEngineTask task;
		if (reportRun.runThenRender) {
			task = engine.createRunTask(design);
		}
		else {
			task = engine.createRunAndRenderTask(design);
		}
		final Map<String, Object> appContext = task.getAppContext();
		task.setAppContext(appContext);
		for (final String key : reportRun.parameters.keySet()) {
			final Object paramValue = reportRun.parameters.get(key);
			final IParameterDefnBase defn = pdTask.getParameterDefn(key);
			final ParameterHandle handle = (ParameterHandle) defn.getHandle();
			final Object dataType = handle.getProperty("dataType");
			logger.info(" param " + key + " = " + paramValue + ", type = " + dataType + " "
				+ defn.getTypeName());
			if (paramValue instanceof Object[]) {
				final Object[] values = (Object[]) paramValue;
				logger.info(" param " + key + " " + values.length);
				for (int i = 0; i < values.length; i++) {
					final Object value = values[i];
					logger.info("   value " + i + " " + value + " " + value.getClass().getName());
					values[i] = convertParameterValue(key + "(" + i + ")", value, dataType);
				}
				task.setParameterValue(key, values);
			}
			else {
				task.setParameterValue(key, convertParameterValue(key, paramValue, dataType));
			}
		}
		logger.info("validating parameters");
		task.validateParameters();
		if (task instanceof IRunAndRenderTask) {
			final IRunAndRenderTask rrTask = (IRunAndRenderTask) task;
			final RenderOption options = getRenderOptions(birtService, reportRun);
			rrTask.setRenderOption(options);
			logger.info("run-and-render report");
			try {
				rrTask.run();
			}
			catch (final UnsupportedFormatException e) {
				throw new BadRequestException(HttpStatus.NOT_ACCEPTABLE,
						"Unsupported output format");
			}
			catch (final Exception e) {
				if ("org.eclipse.birt.report.engine.api.impl.ParameterValidationException".equals(
					e.getClass().getName())) {
					throw new BadRequestException(HttpStatus.NOT_ACCEPTABLE, e.getMessage());
				}
				throw e;
			}
		}
		else if (task instanceof IRunTask) {
			final IRunTask runTask = (IRunTask) task;
			final String outputFilename = reportRun.outputFile;
			final int lastIndexOfDot = outputFilename.lastIndexOf(".");
			String docFilename;
			if (lastIndexOfDot >= 0) {
				docFilename = outputFilename.substring(0, lastIndexOfDot) + ".rptdocument";
			}
			else {
				docFilename = outputFilename + ".rptdocument";
			}
			final File docFile = new File(configService.outputDirectory, docFilename);
			docFile.getParentFile().mkdirs();
			logger.info("run report to " + docFile);
			try {
				runTask.run(docFile.getAbsolutePath());
			}
			catch (final Exception e) {
				logger.info("task.run exception", e);
				if ("org.eclipse.birt.report.engine.api.impl.ParameterValidationException".equals(
					e.getClass().getName())) {
					throw new BadRequestException(HttpStatus.NOT_ACCEPTABLE, e.getMessage());
				}
				throw e;
			}
			final IReportDocument rptdoc = engine.openReportDocument(docFile.getAbsolutePath());
			final IRenderTask renderTask = engine.createRenderTask(rptdoc);
			final RenderOption options = getRenderOptions(birtService, reportRun);
			renderTask.setRenderOption(options);
			final long totalVisiblePageCount = renderTask.getTotalPage();
			renderTask.setPageRange("1-" + totalVisiblePageCount);
			try {
				renderTask.render();
			}
			catch (final UnsupportedFormatException e) {
				throw new BadRequestException(HttpStatus.NOT_ACCEPTABLE,
						"Unsupported output format");
			}
			renderTask.close();
		}
		final List<Exception> exceptions = new ArrayList<>();
		final List<EngineException> errors = task.getErrors();
		if (errors != null) {
			for (final EngineException exception : errors) {
				exceptions.add(exception);
			}
		}
		return exceptions;
	}

	private File getDesignFile(final ReportRun reportRun) {
		File designFile = new File(reportRun.designFile);
		if (!designFile.isAbsolute()) {
			designFile = new File(configService.workspace, reportRun.designFile);
		}
		return designFile;
	}

	private void authorize(final ReportRun reportRun) throws BadRequestException, SQLException {
		final String designFile = getDesignFile(reportRun).getAbsolutePath();
		if (configService.unsecuredDesignFilePattern != null) {
			final Matcher matcher = configService.unsecuredDesignFilePattern.matcher(designFile);
			if (matcher.matches()) {
				return; // allow
			}
		}
		authorizationService.authorize(reportRun.securityToken, designFile);
	}

	/*
	 * Tries to convert from string to whatever is expected
	 */
	private Object convertParameterValue(final String name, final Object paramValue,
			final Object dataType) throws BadRequestException {
		if (paramValue instanceof String) {
			final String stringValue = (String) paramValue;
			if ("integer".equals(dataType)) {
				try {
					return Integer.valueOf(stringValue);
				}
				catch (final NumberFormatException e) {
					logger.error("Parameter " + name + " isn't a valid integer");
					throw new BadRequestException(HttpStatus.NOT_ACCEPTABLE,
							"Parameter " + name + " isn't a valid integer");
				}
			}
			if ("boolean".equals(dataType)) {
				return Boolean.valueOf(stringValue);
			}
			if ("decimal".equals(dataType)) {
				try {
					return Double.valueOf(stringValue);
				}
				catch (final NumberFormatException e) {
					logger.error("Parameter " + name + " isn't a valid decimal");
					throw new BadRequestException(HttpStatus.NOT_ACCEPTABLE,
							"Parameter " + name + " isn't a valid decimal");
				}
			}
			if ("float".equals(dataType)) {
				try {
					return Double.valueOf(stringValue);
				}
				catch (final NumberFormatException e) {
					logger.error("Parameter " + name + " isn't a valid float");
					throw new BadRequestException(HttpStatus.NOT_ACCEPTABLE,
							"Parameter " + name + " isn't a valid float");
				}
			}
			if ("date".equals(dataType)) {
				final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				try {
					return new java.sql.Date(df.parse(stringValue).getTime());
				}
				catch (final ParseException e) {
					logger.error("Parameter " + name + " isn't a valid date");
					throw new BadRequestException(HttpStatus.NOT_ACCEPTABLE,
							"Parameter " + name + " isn't a valid date");
				}
			}
			if ("dateTime".equals(dataType)) {
				final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:dd");
				try {
					return new java.sql.Date(df.parse(stringValue).getTime());
				}
				catch (final ParseException e) {
					logger.error("Parameter " + name + " isn't a valid dateTime");
					throw new BadRequestException(HttpStatus.NOT_ACCEPTABLE,
							"Parameter " + name + " isn't a valid dateTime");
				}
			}
			if ("time".equals(dataType)) {
				final DateFormat df = new SimpleDateFormat("HH:mm:dd");
				try {
					return new java.sql.Time(df.parse(stringValue).getTime());
				}
				catch (final ParseException e) {
					logger.error("Parameter " + name + " isn't a valid time");
					throw new BadRequestException(HttpStatus.NOT_ACCEPTABLE,
							"Parameter " + name + " isn't a valid time");
				}
			}
		}
		return paramValue;
	}

	private RenderOption getRenderOptions(final BirtService env, final ReportRun reportRun) {
		logger.info("getRenderOptions");
		final String format = reportRun.format;
		RenderOption options = null;
		if (format.equalsIgnoreCase(RenderOption.OUTPUT_FORMAT_HTML)) {
			final HTMLRenderOption htmlOption = new HTMLRenderOption();
			htmlOption.setOutputFormat(RenderOption.OUTPUT_FORMAT_HTML);
			htmlOption.setActionHandler(new HTMLActionHandler());
			htmlOption.setImageHandler(new HTMLCompleteImageHandler());
			htmlOption.setBaseImageURL(configService.baseImageURL);
			htmlOption.setImageDirectory("images");
			options = htmlOption;
		}
		if (format.equalsIgnoreCase(RenderOption.OUTPUT_FORMAT_PDF)) {
			options = new PDFRenderOption();
			options.setOutputFormat(RenderOption.OUTPUT_FORMAT_PDF);
		}
		else {
			options = new RenderOption();
			options.setOutputFormat(format.toLowerCase());
		}
		final File outputFile = new File(configService.outputDirectory, reportRun.outputFile);
		logger.info("getRenderOptions outputFile = " + outputFile);
		outputFile.getParentFile().mkdirs();
		options.setOutputFileName(outputFile.getAbsolutePath());
		options.setOutputFormat(format);
		return options;
	}

	@SuppressWarnings("unused")
	private static Object getFieldObject(final String fieldString) {
		if ("true".equalsIgnoreCase(fieldString)) {
			return Boolean.TRUE;
		}
		if ("false".equalsIgnoreCase(fieldString)) {
			return Boolean.FALSE;
		}
		final String trimmedFieldString = fieldString.trim();
		if (trimmedFieldString.startsWith("\"") && trimmedFieldString.endsWith("\"")) {
			return trimmedFieldString.substring(1, trimmedFieldString.length() - 1);
		}
		try {
			final int intValue = Integer.parseInt(fieldString);
			return new Integer(intValue);
		}
		catch (final NumberFormatException e) {
		}
		try {
			final double dblValue = Double.parseDouble(fieldString);
			return new Double(dblValue);
		}
		catch (final NumberFormatException e) {
		}
		try {
			final Date valDate = PARAM_DATE_FORMAT.parse(fieldString);
			return new java.sql.Date(valDate.getTime());
		}
		catch (final ParseException e) {
		}
		return fieldString;
	}

	public static List<File> getPropFiles(final File baseDir) {
		final ArrayList<File> files = new ArrayList<File>();
		if (!baseDir.isDirectory()) {
			files.add(baseDir);
		}
		else {
			final File[] dirFile = baseDir.listFiles(new PropFilter());
			for (int i = 0; i < dirFile.length; i++) {
				files.add(dirFile[i]);
			}
		}
		return files;
	}

	private final static class PropFilter implements FilenameFilter {
		private final String extension = ".properties";

		@Override
		public boolean accept(final File dir, final String name) {
			return name.toLowerCase().endsWith(extension);
		}
	}

	public void waitForAllReportsToFinish() {
		while (true) {
			ReportRunStatus unfinishedReport = null;
			synchronized (reports) {
				for (final UUID uuid : reports.keySet()) {
					final ReportRunStatus status = reports.get(uuid);
					if (!status.isFinished()) {
						unfinishedReport = status;
						break;
					}
				}
				if (unfinishedReport == null) {
					return;
				}
			}
			synchronized (unfinishedReport) {
				while (!unfinishedReport.isFinished()) {
					try {
						unfinishedReport.wait();
					}
					catch (final InterruptedException e) {
					}
				}
			}
		}
	}

	public void shutdown() {
		// there is really no place this can be done
		logger.info("runner shutdown");
		threadPool.shutdown();
	}

	public String getFormat(final String format) {
		if (format == null || format.length() < 1) {
			return configService.reportFormat;
		}
		if (format == null || format.length() < 1) {
			return RenderOption.OUTPUT_FORMAT_PDF;
		}
		return format;
	}

	public ReportRunStatus getStatus(final UUID uuid) {
		return reports.get(uuid);
	}

	public Map<UUID, ReportRunStatus> getStati() {
		return reports;
	}

	public File getOutputDirectory() {
		return configService.outputDirectory;
	}

	public Map<String, Object> fixParameterTypes(final Map<String, Object> parameters)
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
}
