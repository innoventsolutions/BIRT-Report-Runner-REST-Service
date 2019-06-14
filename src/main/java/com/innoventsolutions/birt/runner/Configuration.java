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

public class Configuration {
	public static class Editor {
		File outputDirectory = null;
		File workspace = null;
		File birtRuntimeHome = null;
		File resourcePath = null;
		File scriptLib = null;
		boolean doNotRun = false;
		String reportFormat = null;
		String baseImageURL = null;
		File loggingPropertiesFile = null;
		File loggingDir = null;
		String dbDriver = null;
		String dbUrl = null;
		String dbUsername = null;
		String dbPassword = null;
		String dbQuery = null;
		String mailUsername = null;
		String mailPassword = null;
		File mailPropertiesFile = null;
		String mailFrom = null;
		String mailTo = null;
		String mailCc = null;
		String mailBcc = null;
		String mailSuccessSubject = null;
		String mailFailureSubject = null;
		String mailSuccessBody = null;
		String mailFailureBody = null;
		boolean mailAttachReport = false;
		boolean mailHtml = true;
		int threadCount = 1;
		boolean isActuate = false;

		public void loadProperties(final Properties properties, final File defaultDirectory) {
			final PropertiesHelper ph = new PropertiesHelper(properties);
			outputDirectory = ph.get("birt.runner.outputDir", outputDirectory, defaultDirectory);
			workspace = ph.get("birt.runner.workspace", workspace, defaultDirectory);
			birtRuntimeHome = ph.get("birt.runner.runtime", birtRuntimeHome, defaultDirectory);
			resourcePath = ph.requireFile("birt.runner.resources", defaultDirectory);
			scriptLib = ph.get("birt.runner.scriptlib", scriptLib, defaultDirectory);
			reportFormat = ph.get("birt.runner.reportFormat", reportFormat);
			baseImageURL = ph.get("birt.runner.baseImageURL", baseImageURL);
			loggingPropertiesFile = ph.get("birt.runner.logging.properties", loggingPropertiesFile,
				defaultDirectory);
			loggingDir = ph.get("birt.runner.logging.dir", loggingDir, defaultDirectory);
			dbDriver = ph.get("birt.runner.db.driver", dbDriver);
			dbUrl = ph.get("birt.runner.db.url", dbUrl);
			dbUsername = ph.get("birt.runner.db.username", dbUsername);
			dbPassword = ph.get("birt.runner.db.password", dbPassword);
			dbQuery = ph.get("birt.runner.db.query", dbQuery);
			mailUsername = ph.get("birt.runner.mail.username", mailUsername);
			mailPassword = ph.get("birt.runner.mail.password", mailPassword);
			mailPropertiesFile = ph.get("birt.runner.mail.properties", mailPropertiesFile,
				defaultDirectory);
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
		}
	}

	final File outputDirectory;
	final File workspace;
	final File birtRuntimeHome;
	final File resourcePath;
	final File scriptLib;
	final boolean doNotRun;
	final String reportFormat;
	final String baseImageURL;
	final File loggingPropertiesFile;
	final File loggingDir;
	final String dbDriver;
	final String dbUrl;
	final String dbUsername;
	final String dbPassword;
	final String dbQuery;
	final String mailUsername;
	final String mailPassword;
	final File mailPropertiesFile;
	final String mailTo;
	final String mailCc;
	final String mailBcc;
	final String mailFrom;
	final String mailSuccessSubject;
	final String mailFailureSubject;
	final String mailSuccessBody;
	final String mailFailureBody;
	final boolean mailAttachReport;
	final boolean mailHtml;
	final int threadCount;
	final boolean isActuate;

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
		this.mailUsername = editor.mailUsername;
		this.mailPassword = editor.mailPassword;
		this.mailPropertiesFile = editor.mailPropertiesFile;
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
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("Configuration ");
		sb.append("birtRuntimeHome = " + birtRuntimeHome);
		return sb.toString();
	}
}
