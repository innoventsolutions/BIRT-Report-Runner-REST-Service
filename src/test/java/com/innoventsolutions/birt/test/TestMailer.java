/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.birt.test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Assert;

import com.innoventsolutions.birt.runner.Configuration;
import com.innoventsolutions.birt.runner.Mailer;
import com.innoventsolutions.birt.runner.ReportEmail;
import com.innoventsolutions.birt.runner.ReportRun;
import com.innoventsolutions.birt.runner.ReportRunStatus;

public class TestMailer {
	// @Test
	public void testMailer() {
		final Configuration.Editor editor = new Configuration.Editor();
		editor.mailPropertiesFile = new File(
				this.getClass().getResource("smtp.properties").getPath());
		editor.mailUsername = "foobar";
		editor.mailPassword = "FOOBAR";
		editor.mailFrom = "foobar@foobar.com";
		editor.mailTo = "foobar@foobar.com";
		editor.mailSuccess = true;
		editor.mailSuccessSubject = "Test success";
		editor.mailSuccessBody = "Test success body";
		final Configuration config = new Configuration(editor);
		final Mailer mailer = new Mailer(config);
		final ReportRun reportRun = new ReportRun("test.rptdesign", "Test report", "PDF",
				"output.pdf", true, new HashMap<>(), null);
		final ReportEmail reportEmail = new ReportEmail(true, false, null, null, null, null, null,
				null, null, false, true);
		final ReportRunStatus status = new ReportRunStatus(reportRun, reportEmail);
		status.finishReport(new ArrayList<>());
		mailer.send(status);
		Assert.assertTrue("emailErrors is not empty", status.getEmailErrors().isEmpty());
	}
}
