/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.brr.service;

import java.io.File;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service @Slf4j
public class ConfigService {

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
		@Value("${birt.runner.logging.dir:${user.home}/reportRunnerTest/log}")
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
		@Value("${birt.runner.auth.timeout:5000}")
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
		@Value("${birt.runner.unsecuredDesignFilePattern:}")
		public Pattern unsecuredDesignFilePattern = null;
		@Value("${birt.runner.unsecuredOperationPattern:}")
		public Pattern unsecuredOperationPattern = null;

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
	public final File mailPropertiesFile;
	public final File loggingPropertiesFile;
	public final File loggingDir;
	public final String reportFormat;
	public final String baseImageURL;
	public final Long authTimeout;
	public final String mailUsername;
	public final String mailPassword;
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
	public final Pattern unsecuredOperationPattern;

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
		this.authTimeout = editor.dbTimeout;
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
		this.unsecuredOperationPattern = editor.unsecuredOperationPattern;
		log.info("outputDirectory = " + outputDirectory.getAbsolutePath());
		log.info("workspace = " + workspace.getAbsolutePath());
		log.info("birtRuntimeHome = "
			+ (birtRuntimeHome == null ? "null" : birtRuntimeHome.getAbsolutePath()));
		log.info("resourcePath = " + resourcePath.getAbsolutePath());
		log.info("scriptLib = " + scriptLib.getAbsolutePath());
		log.info("reportFormat = " + reportFormat);
		log.info("baseImageURL = " + baseImageURL);
		log.info("loggingPropertiesFile = " + loggingPropertiesFile.getAbsolutePath());
		log.info("loggingDir = " + loggingDir.getAbsolutePath());
		log.info("dbTimeout = " + authTimeout);
		log.info("mailUsername = " + mailUsername);
		log.info("mailPassword = " + mailPassword);
		log.info("mailPropertiesFile = "
			+ (mailPropertiesFile == null ? "null" : mailPropertiesFile.getAbsolutePath()));
		log.info("mailSuccess = " + mailSuccess);
		log.info("mailFailure = " + mailFailure);
		log.info("mailTo = " + mailTo);
		log.info("mailCc = " + mailCc);
		log.info("mailBcc = " + mailBcc);
		log.info("mailFrom = " + mailFrom);
		log.info("mailSuccessSubject = " + mailSuccessSubject);
		log.info("mailFailureSubject = " + mailFailureSubject);
		log.info("mailSuccessBody = " + mailSuccessBody);
		log.info("mailFailureBody = " + mailFailureBody);
		log.info("mailAttachReport = " + mailAttachReport);
		log.info("mailHtml = " + mailHtml);
		log.info("threadCount = " + threadCount);
		log.info("isActuate = " + isActuate);
		log.info("unsecuredDesignFilePattern = " + unsecuredDesignFilePattern);
		log.info("unsecuredOperationPattern = " + unsecuredOperationPattern);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Configuration ");
		sb.append("birtRuntimeHome = " + birtRuntimeHome);
		return sb.toString();
	}
}
