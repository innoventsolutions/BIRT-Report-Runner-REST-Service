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
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ConfigService {
	Logger logger = LoggerFactory.getLogger(ConfigService.class);

	@Service
	public static class Editor {
		@Value("${birt.runner.outputDirectory:${user.home}/reportRunnerTest/output}")
		public File outputDirectory;
		@Value("${birt.runner.workspace:${user.home}/reportRunnerTest/workspace}")
		public File workspace = null;
		@Value("${birt.runner.runtime:not-specified}")
		public File birtRuntimeHome = null;
		@Value("${birt.runner.resources:${user.home}/reportRunnerTest/resources}")
		public File resourcePath = null;
		@Value("${birt.runner.scriptlib:${user.home}/reportRunnerTest/lib}")
		public File scriptLib = null;
		@Value("${birt.runner.reportFormat:PDF}")
		public String reportFormat = "pdf";
		@Value("${birt.runner.baseImageURL:${user.home}/reportRunnerTest/images}")
		public String baseImageURL = null;
		@Value("${birt.runner.logging.properties:logging.properties}")
		public File loggingPropertiesFile = null;
		@Value("${birt.runner.logging.dir:${user.home}reportRunnerTest/log}")
		public File loggingDir = null;
		@Value("${birt.runner.db.driver:}")
		public String dbDriver = null;
		@Value("${birt.runner.db.url:}")
		public String dbUrl = null;
		@Value("${birt.runner.db.username:}")
		public String dbUsername = null;
		@Value("${birt.runner.db.password:}")
		public String dbPassword = null;
		@Value("${birt.runner.db.query:}")
		public String dbQuery = null;
		@Value("${birt.runner.db.timeout:}")
		public Long dbTimeout = null;
		@Value("${birt.runner.mail.username:}")
		public String mailUsername = null;
		@Value("${birt.runner.mail.password:}")
		public String mailPassword = null;
		@Value("${birt.runner.mail.properties:not-specified}")
		public File mailPropertiesFile = null;
		@Value("${birt.runner.mail.from:}")
		public String mailFrom = null;
		@Value("${birt.runner.mail.success:false}")
		public boolean mailSuccess = false;
		@Value("${birt.runner.mail.failure:false}")
		public boolean mailFailure = false;
		@Value("${birt.runner.mail.to:}")
		public String mailTo = null;
		@Value("${birt.runner.mail.cc:}")
		public String mailCc = null;
		@Value("${birt.runner.mail.bcc:}")
		public String mailBcc = null;
		@Value("${birt.runner.mail.subject.success:}")
		public String mailSuccessSubject = null;
		@Value("${birt.runner.mail.subject.failure:}")
		public String mailFailureSubject = null;
		@Value("${birt.runner.mail.body.success:}")
		public String mailSuccessBody = null;
		@Value("${birt.runner.mail.body.failure:}")
		public String mailFailureBody = null;
		@Value("${birt.runner.mail.attachReport:false}")
		public boolean mailAttachReport = false;
		@Value("${birt.runner.mail.html:true}")
		public boolean mailHtml = true;
		@Value("${birt.runner.threadCount:1}")
		public int threadCount = 1;
		@Value("${birt.runner.isActuate:false}")
		public boolean isActuate = false;
		@Value("${birt.runner.unsecuredDesignFilePattern:.*}")
		public Pattern unsecuredDesignFilePattern = null;

		public void applyDefaults() {
			if (loggingPropertiesFile.isDirectory()) {
				loggingPropertiesFile = new File(loggingPropertiesFile, "logging.properties");
			}
			if (!loggingPropertiesFile.exists()) {
				throw new IllegalArgumentException("Logging properties file "
					+ loggingPropertiesFile.getAbsolutePath() + " does not exist");
			}
			if (mailPropertiesFile.getName().equals("not-specified")) {
				mailPropertiesFile = null;
			}
			else if (mailPropertiesFile.isDirectory()) {
				mailPropertiesFile = new File(mailPropertiesFile, "smtp.properties");
			}
			if (dbDriver.length() == 0) {
				// disables authorization
				dbDriver = null;
			}
			if (birtRuntimeHome.getName().equals("not-specified")) {
				birtRuntimeHome = null;
			}
		}
	}

	public final File outputDirectory;
	public final File workspace;
	public final File birtRuntimeHome;
	public final File resourcePath;
	public final File scriptLib;
	public final String reportFormat;
	public final String baseImageURL;
	public final File loggingPropertiesFile;
	public final File loggingDir;
	public final String dbDriver;
	public final String dbUrl;
	public final String dbUsername;
	public final String dbPassword;
	public final String dbQuery;
	public final Long dbTimeout;
	public final String mailUsername;
	public final String mailPassword;
	public final File mailPropertiesFile;
	public final boolean mailSuccess;
	public final boolean mailFailure;
	public final String mailTo;
	public final String mailCc;
	public final String mailBcc;
	public final String mailFrom;
	public final String mailSuccessSubject;
	public final String mailFailureSubject;
	public final String mailSuccessBody;
	public final String mailFailureBody;
	public final boolean mailAttachReport;
	public final boolean mailHtml;
	public final int threadCount;
	public final boolean isActuate;
	public final Pattern unsecuredDesignFilePattern;

	public ConfigService(@Autowired final Editor editor) {
		editor.applyDefaults();
		this.outputDirectory = editor.outputDirectory;
		this.workspace = editor.workspace;
		this.birtRuntimeHome = editor.birtRuntimeHome;
		this.resourcePath = editor.resourcePath;
		this.scriptLib = editor.scriptLib;
		this.reportFormat = editor.reportFormat;
		this.baseImageURL = editor.baseImageURL;
		this.loggingPropertiesFile = editor.loggingPropertiesFile;
		this.loggingDir = editor.loggingDir;
		this.dbDriver = editor.dbDriver;
		this.dbUrl = editor.dbUrl;
		this.dbUsername = editor.dbUsername;
		this.dbPassword = editor.dbPassword;
		this.dbQuery = editor.dbQuery;
		this.dbTimeout = editor.dbTimeout;
		this.mailUsername = editor.mailUsername;
		this.mailPassword = editor.mailPassword;
		this.mailPropertiesFile = editor.mailPropertiesFile;
		this.mailSuccess = editor.mailSuccess;
		this.mailFailure = editor.mailFailure;
		this.mailTo = editor.mailTo;
		this.mailCc = editor.mailCc;
		this.mailBcc = editor.mailBcc;
		this.mailFrom = editor.mailFrom;
		this.mailSuccessSubject = editor.mailSuccessSubject;
		this.mailFailureSubject = editor.mailFailureSubject;
		this.mailSuccessBody = editor.mailSuccessBody;
		this.mailFailureBody = editor.mailFailureBody;
		this.mailAttachReport = editor.mailAttachReport;
		this.mailHtml = editor.mailHtml;
		this.threadCount = editor.threadCount;
		this.isActuate = editor.isActuate;
		this.unsecuredDesignFilePattern = editor.unsecuredDesignFilePattern;
		logger.info("outputDirectory = " + outputDirectory.getAbsolutePath());
		logger.info("workspace = " + workspace.getAbsolutePath());
		logger.info("birtRuntimeHome = "
			+ (birtRuntimeHome == null ? "null" : birtRuntimeHome.getAbsolutePath()));
		logger.info("resourcePath = " + resourcePath.getAbsolutePath());
		logger.info("scriptLib = " + scriptLib.getAbsolutePath());
		logger.info("reportFormat = " + reportFormat);
		logger.info("baseImageURL = " + baseImageURL);
		logger.info("loggingPropertiesFile = " + loggingPropertiesFile.getAbsolutePath());
		logger.info("loggingDir = " + loggingDir.getAbsolutePath());
		logger.info("dbDriver = " + dbDriver);
		logger.info("dbUrl = " + dbUrl);
		logger.info("dbUsername = " + dbUsername);
		logger.info("dbPassword = " + dbPassword);
		logger.info("dbQuery = " + dbQuery);
		logger.info("dbTimeout = " + dbTimeout);
		logger.info("mailUsername = " + mailUsername);
		logger.info("mailPassword = " + mailPassword);
		logger.info("mailPropertiesFile = "
			+ (mailPropertiesFile == null ? "null" : mailPropertiesFile.getAbsolutePath()));
		logger.info("mailSuccess = " + mailSuccess);
		logger.info("mailFailure = " + mailFailure);
		logger.info("mailTo = " + mailTo);
		logger.info("mailCc = " + mailCc);
		logger.info("mailBcc = " + mailBcc);
		logger.info("mailFrom = " + mailFrom);
		logger.info("mailSuccessSubject = " + mailSuccessSubject);
		logger.info("mailFailureSubject = " + mailFailureSubject);
		logger.info("mailSuccessBody = " + mailSuccessBody);
		logger.info("mailFailureBody = " + mailFailureBody);
		logger.info("mailAttachReport = " + mailAttachReport);
		logger.info("mailHtml = " + mailHtml);
		logger.info("threadCount = " + threadCount);
		logger.info("isActuate = " + isActuate);
		logger.info("unsecuredDesignFilePattern = " + unsecuredDesignFilePattern);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Configuration ");
		sb.append("birtRuntimeHome = " + birtRuntimeHome);
		return sb.toString();
	}
}
