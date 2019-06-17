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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.innoventsolutions.birt.runner.ReportRun;
import com.innoventsolutions.birt.runner.ReportRunStatus;
import com.innoventsolutions.birt.runner.RunnerContext;
import com.innoventsolutions.reportrunneracsbms.ReportRunnerAcSbmsApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = ReportRunnerAcSbmsApplication.class)
public class TestReport {
	Logger logger = LoggerFactory.getLogger(TestReport.class);

	@Test
	public void testOneAsync() {
		final URL propertiesURL = this.getClass().getResource("report-runner.properties");
		final String propertiesFile = propertiesURL.getPath();
		RunnerContext runnerContext;
		try {
			runnerContext = new RunnerContext(propertiesFile);
		}
		catch (final Throwable e) {
			e.printStackTrace();
			Assert.fail("Failed to instantiate RunnerContext: " + e);
			return;
		}
		final URL designURL = this.getClass().getResource("test.rptdesign");
		final String designFile = designURL.getPath();
		final ReportRun reportRun = new ReportRun(designFile, "Test Report", "pdf", "test.pdf",
				true, new HashMap<>(), null);
		try {
			runnerContext.startReport(reportRun, null);
		}
		catch (final Throwable e) {
			e.printStackTrace();
			Assert.fail("Failed to start report: " + e);
			return;
		}
		runnerContext.waitForAllReportsToFinish();
		runnerContext.shutdown();
	}

	@Test
	public void testMultiAsync() {
		final URL propertiesURL = this.getClass().getResource("report-runner.properties");
		final String propertiesFile = propertiesURL.getPath();
		RunnerContext runnerContext;
		try {
			runnerContext = new RunnerContext(propertiesFile);
		}
		catch (final Throwable e) {
			e.printStackTrace();
			Assert.fail("Failed to instantiate RunnerContext: " + e);
			return;
		}
		final URL designURL = this.getClass().getResource("test.rptdesign");
		final String designFile = designURL.getPath();
		final Map<String, Object> parameters = new HashMap<>();
		parameters.put("keyFilter", "a.*");
		final ReportRun[] reportRuns = new ReportRun[] {
			new ReportRun(designFile, "Test Report 1", "pdf", "test1.pdf", true, new HashMap<>(),
					null),
			new ReportRun(designFile, "Test Report 2", "pdf", "test2.pdf", false, new HashMap<>(),
					null),
			new ReportRun(designFile, "Test Report 3", "pdf", "test3.pdf", true, parameters, null),
			new ReportRun(designFile, "Test Report 4", "pdf", "test4.pdf", false, parameters,
					null) };
		final UUID[] uuids = new UUID[reportRuns.length];
		for (int i = 0; i < reportRuns.length; i++) {
			final ReportRun reportRun = reportRuns[i];
			try {
				uuids[i] = runnerContext.startReport(reportRun, null);
			}
			catch (final Throwable e) {
				e.printStackTrace();
				Assert.fail("Failed to start report: " + e);
				return;
			}
		}
		runnerContext.waitForAllReportsToFinish();
		for (final UUID uuid : uuids) {
			final ReportRunStatus status = runnerContext.reports.get(uuid);
			Assert.assertNotNull("Status for " + uuid + " is null", status);
			for (final Exception e : status.getErrors()) {
				logger.info("Exception for " + uuid + " = " + e);
			}
			Assert.assertTrue(status.getErrors().isEmpty());
		}
		runnerContext.shutdown();
	}
}
