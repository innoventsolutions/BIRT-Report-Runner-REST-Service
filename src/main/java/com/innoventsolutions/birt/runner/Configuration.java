/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.birt.runner;

import java.io.File;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * The immutable class that contains all configuration values.
 *
 * @author Steve Schafer, Innovent Solutions Inc.
 * @deprecated Use ConfigService instead.
 */
@Deprecated
public class Configuration {
	/**
	 * The mutable class that contains all configuration values. Values can be
	 * set manually or they can be loaded from a properties object.
	 *
	 * @author Steve Schafer, Innovent Solutions Inc.
	 */
	public static class Editor {
		public File outputDirectory = null;
		public File workspace = null;
		public File birtRuntimeHome = null;
		public File resourcePath = null;
		public File scriptLib = null;
		public boolean doNotRun = false;
		public String reportFormat = "pdf";
		public String baseImageURL = null;
		public File loggingPropertiesFile = null;
		public File loggingDir = null;
		public String dbDriver = null;
		public String dbUrl = null;
		public String dbUsername = null;
		public String dbPassword = null;
		public String dbQuery = null;
		public Long dbTimeout = null;
		public String mailUsername = null;
		public String mailPassword = null;
		public File mailPropertiesFile = null;
		public String mailFrom = null;
		public boolean mailSuccess = false;
		public boolean mailFailure = false;
		public String mailTo = null;
		public String mailCc = null;
		public String mailBcc = null;
		public String mailSuccessSubject = null;
		public String mailFailureSubject = null;
		public String mailSuccessBody = null;
		public String mailFailureBody = null;
		public boolean mailAttachReport = false;
		public boolean mailHtml = true;
		public int threadCount = 1;
		public boolean isActuate = false;
		public Pattern unsecuredDesignFilePattern = null;

		/**
		 * Load the properties from a Properties object. All fields are public.
		 * Any values set to these fields prior to calling this method will
		 * serve as defaults.
		 *
		 * @param properties
		 *            The properties object
		 * @param propertiesDir
		 *            The default location for some properties
		 */
		public void loadProperties(final Properties properties, final File propertiesDir) {
			final String userHome = System.getProperty("user.home");
			final File userHomeDir = userHome == null ? null : new File(userHome);
			final File defaultWorkDir = new File(userHomeDir, "reportRunnerTest");
			final PropertiesHelper ph = new PropertiesHelper(properties);
			File defaultOutputDirectory = outputDirectory;
			if (defaultOutputDirectory == null) {
				defaultOutputDirectory = new File(defaultWorkDir, "output");
			}
			outputDirectory = ph.get("birt.runner.outputDir", defaultOutputDirectory, userHomeDir);
			File defaultWorkspace = workspace;
			if (defaultWorkspace == null) {
				defaultWorkspace = new File(defaultWorkDir, "reports");
			}
			workspace = ph.get("birt.runner.workspace", defaultWorkspace, userHomeDir);
			birtRuntimeHome = ph.get("birt.runner.runtime", birtRuntimeHome, propertiesDir);
			File defaultResourcePath = resourcePath;
			if (defaultResourcePath == null) {
				defaultResourcePath = new File(propertiesDir, "resources");
			}
			resourcePath = ph.get("birt.runner.resources", defaultResourcePath, propertiesDir);
			File defaultScriptLib = scriptLib;
			if (defaultScriptLib == null) {
				defaultScriptLib = new File(propertiesDir, "lib");
			}
			scriptLib = ph.get("birt.runner.scriptlib", defaultScriptLib, propertiesDir);
			reportFormat = ph.get("birt.runner.reportFormat", reportFormat);
			baseImageURL = ph.get("birt.runner.baseImageURL", baseImageURL);
			File defaultLoggingPropertiesFile = loggingPropertiesFile;
			if (defaultLoggingPropertiesFile == null) {
				defaultLoggingPropertiesFile = new File(propertiesDir, "logging.properties");
			}
			loggingPropertiesFile = ph.get("birt.runner.logging.properties",
				defaultLoggingPropertiesFile, propertiesDir);
			File defaultLoggingDir = loggingDir;
			if (defaultLoggingDir == null) {
				defaultLoggingDir = new File(defaultWorkDir, "log");
			}
			loggingDir = ph.get("birt.runner.logging.dir", defaultLoggingDir, propertiesDir);
			dbDriver = ph.get("birt.runner.db.driver", dbDriver);
			dbUrl = ph.get("birt.runner.db.url", dbUrl);
			dbUsername = ph.get("birt.runner.db.username", dbUsername);
			dbPassword = ph.get("birt.runner.db.password", dbPassword);
			dbQuery = ph.get("birt.runner.db.query", dbQuery);
			Long defaultDbTimeout = dbTimeout;
			if (defaultDbTimeout == null) {
				defaultDbTimeout = Long.valueOf(5000);
			}
			dbTimeout = ph.get("birt.runner.db.timeout", defaultDbTimeout);
			mailUsername = ph.get("birt.runner.mail.username", mailUsername);
			mailPassword = ph.get("birt.runner.mail.password", mailPassword);
			File defaultMailPropertiesFile = mailPropertiesFile;
			if (defaultMailPropertiesFile == null) {
				defaultMailPropertiesFile = new File(propertiesDir, "smtp.properties");
			}
			mailPropertiesFile = ph.get("birt.runner.mail.properties", defaultMailPropertiesFile,
				propertiesDir);
			mailSuccess = ph.get("birt.runner.mail.success", mailSuccess);
			mailFailure = ph.get("birt.runner.mail.failure", mailFailure);
			mailTo = ph.get("birt.runner.mail.to", mailTo);
			mailCc = ph.get("birt.runner.mail.cc", mailCc);
			mailBcc = ph.get("birt.runner.mail.bcc", mailBcc);
			mailFrom = ph.get("birt.runner.mail.from", mailFrom);
			mailSuccessSubject = ph.get("birt.runner.mail.subject.success", mailSuccessSubject);
			mailFailureSubject = ph.get("birt.runner.mail.subject.failure", mailFailureSubject);
			mailSuccessBody = ph.get("birt.runner.mail.body.success", mailSuccessBody);
			mailFailureBody = ph.get("birt.runner.mail.body.failure", mailFailureBody);
			mailAttachReport = ph.get("birt.runner.mail.attachReport", true);
			mailHtml = ph.get("birt.runner.mail.html", true);
			threadCount = ph.get("birt.runner.threadCount", threadCount);
			isActuate = ph.get("birt.runner.isActuate", isActuate);
			final String patternString = ph.get("birt.runner.unsecuredDesignFilePattern",
				unsecuredDesignFilePattern == null ? null : unsecuredDesignFilePattern.pattern());
			if (patternString != null) {
				unsecuredDesignFilePattern = Pattern.compile(patternString);
			}
			else {
				unsecuredDesignFilePattern = null;
			}
		}
	}

	public final File outputDirectory;
	public final File workspace;
	public final File birtRuntimeHome;
	public final File resourcePath;
	public final File scriptLib;
	public final boolean doNotRun;
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

	public Configuration(final Editor editor) {
		this.outputDirectory = editor.outputDirectory;
		this.workspace = editor.workspace;
		this.birtRuntimeHome = editor.birtRuntimeHome;
		this.resourcePath = editor.resourcePath;
		this.scriptLib = editor.scriptLib;
		this.doNotRun = editor.doNotRun;
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
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Configuration ");
		sb.append("birtRuntimeHome = " + birtRuntimeHome);
		return sb.toString();
	}
}
