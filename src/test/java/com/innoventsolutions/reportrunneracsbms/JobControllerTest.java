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

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ReportRunnerAcSbmsApplication.class)
@WebAppConfiguration
public class JobControllerTest {
	private static final Object DESIGN_FILE_DESCRIPTION = "The full path to the BIRT design file on the server file system";
	private static final Object FORMAT_DESCRIPTION = "The report output format: HTML, PDF, XLS, or any other format supported by the BIRT engine";
	private static final Object RUN_THEN_RENDER_DESCRIPTION = "Whether to build with separate run and render phases";
	private static final Object PARAMETERS_DESCRIPTION = "The parameters in the form {\"name\": value, ...}, where value may be a string, number or boolean for single value parameters or an array of string, number, or boolean for multi-valued parameters.";
	private static final Object TOKEN_DESCRIPTION = "The security token for this request.  This is required only if database security has been enabled in the configuration.";
	private static final Object JOB_ID_DESCRIPTION = "The job ID";
	private static final Object JOB_GROUP_DESCRIPTION = "The job group name";
	private static final Object JOB_NAME_DESCRIPTION = "The job name";
	private static final Object JOB_START_DATE_DESCRIPTION = "The start date and time as dd-MM-yyyy hh:mm:ss";
	Logger logger = LoggerFactory.getLogger(JobControllerTest.class);
	@Rule
	public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation(
			"target/generated-snippets");
	@Autowired
	private WebApplicationContext context;
	private MockMvc mockMvc;

	@Before
	public void setUp() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).apply(
			documentationConfiguration(this.restDocumentation)).build();
	}

	static class TestRunRequestParams extends TestRunRequestNoparams {
		Map<String, Object> parameters;

		public Map<String, Object> getParameters() {
			return parameters;
		}

		public void setParameters(final Map<String, Object> parameters) {
			this.parameters = parameters;
		}
	}

	@Test
	public void testRun() throws Exception {
		final TestRunRequestParams requestObject = new TestRunRequestParams();
		final URL designFileURL = this.getClass().getResource("test.rptdesign");
		requestObject.designFile = designFileURL.getPath();
		requestObject.format = "pdf";
		requestObject.runThenRender = true;
		requestObject.parameters = new HashMap<>();
		requestObject.parameters.put("keyFilter", "a.*");
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(requestObject);
		logger.info("testRun request = " + requestString);
		this.mockMvc.perform(
			post("/run").contentType(MediaType.APPLICATION_JSON).content(requestString).accept(
				MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andDo(
					document("run",
						requestFields(
							fieldWithPath("designFile").description(DESIGN_FILE_DESCRIPTION),
							fieldWithPath("format").optional().description(FORMAT_DESCRIPTION),
							fieldWithPath("runThenRender").optional().description(
								RUN_THEN_RENDER_DESCRIPTION),
							subsectionWithPath("parameters").optional().type(
								JsonFieldType.OBJECT).description(PARAMETERS_DESCRIPTION),
							fieldWithPath("securityToken").type(
								JsonFieldType.STRING).optional().description(TOKEN_DESCRIPTION)),
						responseHeaders(headerWithName("Content-Type").description(
							"The content type of the payload"))));
	}

	static class TestRunRequestNoparams {
		String designFile;
		String format;
		boolean runThenRender;

		public String getDesignFile() {
			return designFile;
		}

		public void setDesignFile(final String designFile) {
			this.designFile = designFile;
		}

		public String getFormat() {
			return format;
		}

		public void setFormat(final String format) {
			this.format = format;
		}

		public boolean isRunThenRender() {
			return runThenRender;
		}

		public void setRunThenRender(final boolean runThenRender) {
			this.runThenRender = runThenRender;
		}
	}

	@Test
	public void testRunNoParams() throws Exception {
		final TestRunRequestNoparams requestObject = new TestRunRequestNoparams();
		final URL designFileURL = this.getClass().getResource("test.rptdesign");
		requestObject.designFile = designFileURL.getPath();
		requestObject.format = "pdf";
		requestObject.runThenRender = true;
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(requestObject);
		logger.info("testRunNoParams request = " + requestString);
		this.mockMvc.perform(
			post("/run").contentType(MediaType.APPLICATION_JSON).content(requestString).accept(
				MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}

	@Test
	public void testRunRowCount() throws Exception {
		final TestRunRequestParams requestObject = new TestRunRequestParams();
		final URL designFileURL = this.getClass().getResource("test.rptdesign");
		requestObject.designFile = designFileURL.getPath();
		requestObject.format = "pdf";
		requestObject.runThenRender = true;
		requestObject.parameters = new HashMap<>();
		requestObject.parameters.put("rowCount", "5");
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(requestObject);
		logger.info("testRunRowCount request = " + requestString);
		this.mockMvc.perform(
			post("/run").contentType(MediaType.APPLICATION_JSON).content(requestString).accept(
				MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}

	static class TestSubmitRequestNoparams extends TestRunRequestNoparams {
		String nameForHumans;

		public String getNameForHumans() {
			return nameForHumans;
		}

		public void setNameForHumans(final String nameForHumans) {
			this.nameForHumans = nameForHumans;
		}
	}

	@Test
	public void testSubmit() throws Exception {
		final TestSubmitRequestNoparams requestObject = new TestSubmitRequestNoparams();
		final URL designFileURL = this.getClass().getResource("test.rptdesign");
		requestObject.designFile = designFileURL.getPath();
		requestObject.format = "PDF";
		requestObject.runThenRender = true;
		requestObject.nameForHumans = "Test Report";
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(requestObject);
		logger.info("testSubmit request = " + requestString);
		final MvcResult result = this.mockMvc.perform(
			post("/submit").contentType(MediaType.APPLICATION_JSON).content(requestString).accept(
				MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andDo(document(
					"submit",
					requestFields(fieldWithPath("designFile").description(DESIGN_FILE_DESCRIPTION),
						fieldWithPath("format").optional().description(FORMAT_DESCRIPTION),
						fieldWithPath("runThenRender").optional().description(
							RUN_THEN_RENDER_DESCRIPTION),
						subsectionWithPath("parameters").optional().type(
							JsonFieldType.OBJECT).description(PARAMETERS_DESCRIPTION),
						fieldWithPath("securityToken").type(
							JsonFieldType.STRING).optional().description(TOKEN_DESCRIPTION),
						fieldWithPath("nameForHumans").optional().description(
							"A human friendly name for the report.  This will appear in a report status message and emails."),
						fieldWithPath("sendEmailOnSuccess").type(
							JsonFieldType.BOOLEAN).optional().description(
								"Indicates whether or not email should be sent for successful generation"),
						fieldWithPath("sendEmailOnFailure").type(
							JsonFieldType.BOOLEAN).optional().description(
								"Indicates whether or not email should be sent for failed generation"),
						fieldWithPath("mailTo").type(JsonFieldType.STRING).optional().description(
							"Recipient addresses separated by commas"),
						fieldWithPath("mailCc").type(JsonFieldType.STRING).optional().description(
							"Copy-recipient addresses separated by commas"),
						fieldWithPath("mailBcc").type(JsonFieldType.STRING).optional().description(
							"Blind-copy-addresses separated by commas"),
						fieldWithPath("mailSuccessSubject").type(
							JsonFieldType.STRING).optional().description(
								"Email subject for successful reports"),
						fieldWithPath("mailFailureSubject").type(
							JsonFieldType.STRING).optional().description(
								"Email subject for failed reports"),
						fieldWithPath("mailSuccessBody").type(
							JsonFieldType.STRING).optional().description(
								"Email body for successful reports"),
						fieldWithPath("mailFailureBody").type(
							JsonFieldType.STRING).optional().description(
								"Email body for failed reports")),
					responseFields(
						fieldWithPath("exception").description(
							"Returns the exception object in case of failure"),
						fieldWithPath("exceptionString").description(
							"Returns the exception message string in case of failure"),
						fieldWithPath("success").description(
							"Returns true if the request was successfully processed"),
						fieldWithPath("uuid").description(
							"Returns the UUID that identifies the job")))).andReturn();
		final MockHttpServletResponse response = result.getResponse();
		Assert.assertTrue(response.getContentType().startsWith("application/json"));
		final String jsonString = response.getContentAsString();
		logger.info("testSubmit response = " + jsonString);
		@SuppressWarnings("unchecked")
		final Map<String, String> submitResponse = mapper.readValue(jsonString, Map.class);
		final String jobId = submitResponse.get("uuid");
		logger.info("testSubmit jobId = " + jobId);
		final String exceptionString = submitResponse.get("exceptionString");
		logger.info("testSubmit exceptionString = " + exceptionString);
		Assert.assertFalse("Exception string is not null and not blank: " + exceptionString,
			exceptionString != null && exceptionString.trim().length() > 0);
		Assert.assertNotNull("Job ID is null", jobId);
	}

	public String submit() throws Exception {
		final TestSubmitRequestNoparams requestObject = new TestSubmitRequestNoparams();
		final URL designFileURL = this.getClass().getResource("test.rptdesign");
		requestObject.designFile = designFileURL.getPath();
		requestObject.format = "PDF";
		requestObject.runThenRender = true;
		requestObject.nameForHumans = "Test Report";
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(requestObject);
		logger.info("submit request = " + requestString);
		final MvcResult result = this.mockMvc.perform(
			post("/submit").contentType(MediaType.APPLICATION_JSON).content(requestString).accept(
				MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
		final MockHttpServletResponse response = result.getResponse();
		Assert.assertTrue(response.getContentType().startsWith("application/json"));
		final String jsonString = response.getContentAsString();
		logger.info("testStatus submit response = " + jsonString);
		@SuppressWarnings("unchecked")
		final Map<String, String> submitResponse = mapper.readValue(jsonString, Map.class);
		final String jobId = submitResponse.get("uuid");
		logger.info("testStatus jobId = " + jobId);
		final String exceptionString = submitResponse.get("exceptionString");
		logger.info("testStatus exceptionString = " + exceptionString);
		Assert.assertFalse("Exception string is not null and not blank: " + exceptionString,
			exceptionString != null && exceptionString.trim().length() > 0);
		Assert.assertNotNull("Job ID is null", jobId);
		return jobId;
	}

	@Test
	public void testStatus() throws Exception {
		final StatusRequest request = new StatusRequest();
		request.setSecurityToken(null);
		request.setJobId(submit());
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(request);
		logger.info("status request = " + requestString);
		final MvcResult statusResult = this.mockMvc.perform(
			get("/status").contentType(MediaType.APPLICATION_JSON).content(requestString).accept(
				MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andDo(document(
					"status",
					requestFields(fieldWithPath("jobId").description(JOB_ID_DESCRIPTION),
						fieldWithPath("securityToken").type(
							JsonFieldType.STRING).optional().description(TOKEN_DESCRIPTION)),
					responseFields(
						subsectionWithPath("reportRun").description("The report run request"),
						subsectionWithPath("email").description("The email request"),
						fieldWithPath("startTime").description(
							"The time when the report generation started"),
						fieldWithPath("finishTime").description(
							"The time when report generation and email finished or null if not finished"),
						fieldWithPath("reportFinishTime").description(
							"The time when the report generation finished or null if it is still running"),
						fieldWithPath("duration").description(
							"The number of milliseconds it took to generate the report"),
						fieldWithPath("finished").description(
							"Whether the report generation is finished"),
						subsectionWithPath("errors").description(
							"List of exceptions that were encountered during report generation"),
						subsectionWithPath("emailErrors").description(
							"List of exceptions that were encountered during sending email")))).andReturn();
		final MockHttpServletResponse response2 = statusResult.getResponse();
		Assert.assertTrue(response2.getContentType().startsWith("application/json"));
		final String jsonString2 = response2.getContentAsString();
		logger.info("testStatus response = " + jsonString2);
		@SuppressWarnings("unchecked")
		final Map<String, Object> responseMap = mapper.readValue(jsonString2, Map.class);
		logger.info("responseMap = " + responseMap);
		@SuppressWarnings("unchecked")
		final List<Object> reportErrors = (List<Object>) responseMap.get("errors");
		Assert.assertNotNull("reportErrors should not be null", reportErrors);
		Assert.assertTrue("reportErrors should be empty", reportErrors.isEmpty());
		@SuppressWarnings("unchecked")
		final Map<String, Object> emailErrors = (Map<String, Object>) responseMap.get(
			"emailErrors");
		Assert.assertNotNull("emailErrors should not be null", emailErrors);
		Assert.assertTrue("emailErrors should be empty", emailErrors.isEmpty());
	}

	@Test
	public void testWaitFor() throws Exception {
		final WaitforRequest request = new WaitforRequest();
		request.setSecurityToken(null);
		request.setJobId(submit());
		request.setTimeout(Long.valueOf(5000L));
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(request);
		logger.info("status request = " + requestString);
		final MvcResult statusResult = this.mockMvc.perform(
			get("/waitfor").contentType(MediaType.APPLICATION_JSON).content(requestString).accept(
				MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andDo(document(
					"waitfor",
					requestFields(fieldWithPath("jobId").description(JOB_ID_DESCRIPTION),
						fieldWithPath("securityToken").type(
							JsonFieldType.STRING).optional().description(TOKEN_DESCRIPTION),
						fieldWithPath("timeout").description(
							"The maximum number of milliseconds to wait")),
					responseFields(
						subsectionWithPath("reportRun").description("The report run request"),
						subsectionWithPath("email").description("The email request"),
						fieldWithPath("startTime").description(
							"The time when the report generation started"),
						fieldWithPath("finishTime").description(
							"The time when report generation and email finished or null if not finished"),
						fieldWithPath("reportFinishTime").description(
							"The time when the report generation finished or null if it is still running"),
						fieldWithPath("duration").description(
							"The number of milliseconds it took to generate the report"),
						fieldWithPath("finished").description(
							"Whether the report generation is finished"),
						subsectionWithPath("errors").description(
							"List of exceptions that were encountered during report generation"),
						subsectionWithPath("emailErrors").description(
							"List of exceptions that were encountered during sending email")))).andReturn();
		final MockHttpServletResponse response2 = statusResult.getResponse();
		Assert.assertTrue(response2.getContentType().startsWith("application/json"));
		final String jsonString2 = response2.getContentAsString();
		logger.info("testStatus response = " + jsonString2);
		@SuppressWarnings("unchecked")
		final Map<String, Object> responseMap = mapper.readValue(jsonString2, Map.class);
		logger.info("responseMap = " + responseMap);
		@SuppressWarnings("unchecked")
		final List<Object> reportErrors = (List<Object>) responseMap.get("errors");
		Assert.assertNotNull("reportErrors should not be null", reportErrors);
		Assert.assertTrue("reportErrors should be empty", reportErrors.isEmpty());
		@SuppressWarnings("unchecked")
		final Map<String, Object> emailErrors = (Map<String, Object>) responseMap.get(
			"emailErrors");
		Assert.assertNotNull("emailErrors should not be null", emailErrors);
		Assert.assertTrue("emailErrors should be empty", emailErrors.isEmpty());
	}

	@Test
	public void testStatusAll() throws Exception {
		final BaseRequest request = new BaseRequest();
		request.setSecurityToken(null);
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(request);
		logger.info("status-all request = " + requestString);
		final MvcResult statusResult = this.mockMvc.perform(
			get("/status-all").contentType(MediaType.APPLICATION_JSON).content(
				requestString).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andDo(
					document("status-all",
						requestFields(fieldWithPath("securityToken").type(
							JsonFieldType.STRING).optional().description(TOKEN_DESCRIPTION)),
						relaxedResponseFields())).andReturn();
		final MockHttpServletResponse response2 = statusResult.getResponse();
		Assert.assertTrue(response2.getContentType().startsWith("application/json"));
		final String jsonString2 = response2.getContentAsString();
		logger.info("testStatusAll response = " + jsonString2);
		@SuppressWarnings("unchecked")
		final Map<String, Object> responseMap = mapper.readValue(jsonString2, Map.class);
		logger.info("responseMap = " + responseMap);
	}

	@Test
	public void testGet() throws Exception {
		final StatusRequest request = new StatusRequest();
		request.setSecurityToken(null);
		request.setJobId(submit());
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(request);
		logger.info("get request = " + requestString);
		this.mockMvc.perform(
			get("/get").contentType(MediaType.APPLICATION_JSON).content(requestString).accept(
				MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andDo(
					document("get",
						requestFields(fieldWithPath("jobId").description(JOB_ID_DESCRIPTION),
							fieldWithPath("securityToken").type(
								JsonFieldType.STRING).optional().description(TOKEN_DESCRIPTION)),
						responseHeaders(headerWithName("Content-Type").description(
							"The content type of the payload")))).andReturn();
	}

	@Test
	public void testDownload() throws Exception {
		final StatusRequest request = new StatusRequest();
		request.setSecurityToken(null);
		request.setJobId(submit());
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(request);
		logger.info("get request = " + requestString);
		this.mockMvc.perform(
			get("/download").contentType(MediaType.APPLICATION_JSON).content(requestString).accept(
				MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andDo(
					document("download",
						requestFields(fieldWithPath("jobId").description(JOB_ID_DESCRIPTION),
							fieldWithPath("securityToken").type(
								JsonFieldType.STRING).optional().description(TOKEN_DESCRIPTION)),
						responseHeaders(headerWithName("Content-Type").description(
							"The content type of the payload")))).andReturn();
	}

	static class TestScheduleRequest {
		String group;
		String name;
		Date startDate;
		TestSubmitRequestNoparams submit;

		public String getGroup() {
			return group;
		}

		public void setGroup(final String group) {
			this.group = group;
		}

		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
		public Date getStartDate() {
			return startDate;
		}

		@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
		public void setStartDate(final Date startDate) {
			this.startDate = startDate;
		}

		public TestSubmitRequestNoparams getSubmit() {
			return submit;
		}

		public void setSubmit(final TestSubmitRequestNoparams submit) {
			this.submit = submit;
		}
	}

	static class TestScheduleSimpleRequest extends TestScheduleRequest {
		Long intervalInMilliseconds;
		Integer repeatCount;
		String misfireInstruction;

		public Long getIntervalInMilliseconds() {
			return intervalInMilliseconds;
		}

		public void setIntervalInMilliseconds(final Long intervalInMilliseconds) {
			this.intervalInMilliseconds = intervalInMilliseconds;
		}

		public Integer getRepeatCount() {
			return repeatCount;
		}

		public void setRepeatCount(final Integer repeatCount) {
			this.repeatCount = repeatCount;
		}

		public String getMisfireInstruction() {
			return misfireInstruction;
		}

		public void setMisfireInstruction(final String misfireInstruction) {
			this.misfireInstruction = misfireInstruction;
		}
	}

	@Test
	public void testScheduleSimple() throws Exception {
		final TestScheduleSimpleRequest requestObject = new TestScheduleSimpleRequest();
		final URL designFileURL = this.getClass().getResource("test.rptdesign");
		requestObject.submit = new TestSubmitRequestNoparams();
		requestObject.submit.designFile = designFileURL.getPath();
		requestObject.submit.format = "PDF";
		requestObject.submit.runThenRender = true;
		requestObject.submit.nameForHumans = "Test Report";
		requestObject.group = "simple-test";
		requestObject.name = "simple-test";
		requestObject.startDate = null;
		requestObject.intervalInMilliseconds = Long.valueOf(1000L);
		requestObject.repeatCount = 1;
		requestObject.misfireInstruction = null;
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(requestObject);
		logger.info("testScheduleSimple request = " + requestString);
		final MvcResult result = this.mockMvc.perform(
			post("/schedule-simple").contentType(MediaType.APPLICATION_JSON).content(
				requestString).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andDo(
					document("schedule-simple", requestFields(
						fieldWithPath("group").description(JOB_GROUP_DESCRIPTION),
						fieldWithPath("name").description(JOB_NAME_DESCRIPTION),
						fieldWithPath("startDate").description(JOB_START_DATE_DESCRIPTION),
						fieldWithPath("intervalInMilliseconds").optional().description(
							"The interval in milliseconds between runs"),
						fieldWithPath("repeatCount").optional().description(
							"The number of times to repeat the run. The default is to repeat forever."),
						fieldWithPath("misfireInstruction").optional().description(
							"One of 'ignore', 'fire-now', 'next-with-existing-count', 'next-with-remaining-count', 'now-with-existing-count', or 'now-with-remaining-count'"),
						fieldWithPath("securityToken").type(
							JsonFieldType.STRING).optional().description(TOKEN_DESCRIPTION),
						fieldWithPath("submit.designFile").description(DESIGN_FILE_DESCRIPTION),
						fieldWithPath("submit.format").optional().description(FORMAT_DESCRIPTION),
						fieldWithPath("submit.runThenRender").optional().description(
							RUN_THEN_RENDER_DESCRIPTION),
						subsectionWithPath("submit.parameters").optional().type(
							JsonFieldType.OBJECT).description(PARAMETERS_DESCRIPTION),
						fieldWithPath("submit.nameForHumans").optional().description(
							"A human friendly name for the report.  This will appear in a report status message and emails."),
						fieldWithPath("submit.sendEmailOnSuccess").type(
							JsonFieldType.BOOLEAN).optional().description(
								"Indicates whether or not email should be sent for successful generation"),
						fieldWithPath("submit.sendEmailOnFailure").type(
							JsonFieldType.BOOLEAN).optional().description(
								"Indicates whether or not email should be sent for failed generation"),
						fieldWithPath("submit.mailTo").type(
							JsonFieldType.STRING).optional().description(
								"Recipient addresses separated by commas"),
						fieldWithPath("submit.mailCc").type(
							JsonFieldType.STRING).optional().description(
								"Copy-recipient addresses separated by commas"),
						fieldWithPath("submit.mailBcc").type(
							JsonFieldType.STRING).optional().description(
								"Blind-copy-addresses separated by commas"),
						fieldWithPath("submit.mailSuccessSubject").type(
							JsonFieldType.STRING).optional().description(
								"Email subject for successful reports"),
						fieldWithPath("submit.mailFailureSubject").type(
							JsonFieldType.STRING).optional().description(
								"Email subject for failed reports"),
						fieldWithPath("submit.mailSuccessBody").type(
							JsonFieldType.STRING).optional().description(
								"Email body for successful reports"),
						fieldWithPath("submit.mailFailureBody").type(
							JsonFieldType.STRING).optional().description(
								"Email body for failed reports")),
						responseFields(
							fieldWithPath("message").description(
								"Returns the exception message string in case of failure"),
							fieldWithPath("jobKey.group").description(
								"The job group name passed in the request"),
							fieldWithPath("jobKey.name").description(
								"The job name passed in the request")))).andReturn();
		final MockHttpServletResponse response = result.getResponse();
		Assert.assertTrue(response.getContentType().startsWith("application/json"));
		final String jsonString = response.getContentAsString();
		logger.info("testScheduleSimple response = " + jsonString);
		@SuppressWarnings("unchecked")
		final Map<String, Object> scheduleResponse = mapper.readValue(jsonString, Map.class);
		@SuppressWarnings("unchecked")
		final Map<String, String> jobKey = (Map<String, String>) scheduleResponse.get("jobKey");
		logger.info("testScheduleSimple jobKey = " + jobKey);
		final String message = (String) scheduleResponse.get("message");
		logger.info("testScheduleSimple message = " + message);
		Assert.assertFalse("Message is not null and not blank: " + message,
			message != null && message.trim().length() > 0);
		Assert.assertNotNull("Job key is null", jobKey);
	}

	static class TestScheduleCronRequest extends TestScheduleRequest {
		String cronString;
		String misfireInstruction;

		public String getCronString() {
			return cronString;
		}

		public void setCronString(final String cronString) {
			this.cronString = cronString;
		}

		public String getMisfireInstruction() {
			return misfireInstruction;
		}

		public void setMisfireInstruction(final String misfireInstruction) {
			this.misfireInstruction = misfireInstruction;
		}
	}

	@Test
	public void testScheduleCron() throws Exception {
		final TestScheduleCronRequest requestObject = new TestScheduleCronRequest();
		final URL designFileURL = this.getClass().getResource("test.rptdesign");
		requestObject.submit = new TestSubmitRequestNoparams();
		requestObject.submit.designFile = designFileURL.getPath();
		requestObject.submit.format = "PDF";
		requestObject.submit.runThenRender = true;
		requestObject.submit.nameForHumans = "Test Report";
		requestObject.group = "cron-test";
		requestObject.name = "cron-test";
		requestObject.startDate = null;
		final long time = System.currentTimeMillis() + 31L * 24L * 60L * 60L * 1000L;
		logger.info("cron time = " + new Date(time));
		requestObject.cronString = getCronString(time);
		requestObject.misfireInstruction = null;
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(requestObject);
		logger.info("testScheduleCron request = " + requestString);
		final MvcResult result = this.mockMvc.perform(
			post("/schedule-cron").contentType(MediaType.APPLICATION_JSON).content(
				requestString).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andDo(
					document("schedule-cron", requestFields(
						fieldWithPath("group").description(JOB_GROUP_DESCRIPTION),
						fieldWithPath("name").description(JOB_NAME_DESCRIPTION),
						fieldWithPath("startDate").description(JOB_START_DATE_DESCRIPTION),
						fieldWithPath("cronString").description(
							"The cron string as described in cron documentation"),
						fieldWithPath("misfireInstruction").optional().description(
							"One of 'ignore', 'fire-and-proceed', or 'do-nothing'"),
						fieldWithPath("securityToken").type(
							JsonFieldType.STRING).optional().description(TOKEN_DESCRIPTION),
						fieldWithPath("submit.designFile").description(DESIGN_FILE_DESCRIPTION),
						fieldWithPath("submit.format").optional().description(FORMAT_DESCRIPTION),
						fieldWithPath("submit.runThenRender").optional().description(
							RUN_THEN_RENDER_DESCRIPTION),
						subsectionWithPath("submit.parameters").optional().type(
							JsonFieldType.OBJECT).description(PARAMETERS_DESCRIPTION),
						fieldWithPath("submit.nameForHumans").optional().description(
							"A human friendly name for the report.  This will appear in a report status message and emails."),
						fieldWithPath("submit.sendEmailOnSuccess").type(
							JsonFieldType.BOOLEAN).optional().description(
								"Indicates whether or not email should be sent for successful generation"),
						fieldWithPath("submit.sendEmailOnFailure").type(
							JsonFieldType.BOOLEAN).optional().description(
								"Indicates whether or not email should be sent for failed generation"),
						fieldWithPath("submit.mailTo").type(
							JsonFieldType.STRING).optional().description(
								"Recipient addresses separated by commas"),
						fieldWithPath("submit.mailCc").type(
							JsonFieldType.STRING).optional().description(
								"Copy-recipient addresses separated by commas"),
						fieldWithPath("submit.mailBcc").type(
							JsonFieldType.STRING).optional().description(
								"Blind-copy-addresses separated by commas"),
						fieldWithPath("submit.mailSuccessSubject").type(
							JsonFieldType.STRING).optional().description(
								"Email subject for successful reports"),
						fieldWithPath("submit.mailFailureSubject").type(
							JsonFieldType.STRING).optional().description(
								"Email subject for failed reports"),
						fieldWithPath("submit.mailSuccessBody").type(
							JsonFieldType.STRING).optional().description(
								"Email body for successful reports"),
						fieldWithPath("submit.mailFailureBody").type(
							JsonFieldType.STRING).optional().description(
								"Email body for failed reports")),
						responseFields(
							fieldWithPath("message").description(
								"Returns the exception message string in case of failure"),
							fieldWithPath("jobKey.group").description(
								"The job group name passed in the request"),
							fieldWithPath("jobKey.name").description(
								"The job name passed in the request")))).andReturn();
		final MockHttpServletResponse response = result.getResponse();
		Assert.assertTrue(response.getContentType().startsWith("application/json"));
		final String jsonString = response.getContentAsString();
		logger.info("testScheduleCron response = " + jsonString);
		@SuppressWarnings("unchecked")
		final Map<String, Object> scheduleResponse = mapper.readValue(jsonString, Map.class);
		@SuppressWarnings("unchecked")
		final Map<String, String> jobKey = (Map<String, String>) scheduleResponse.get("jobKey");
		logger.info("testScheduleCron jobKey = " + jobKey);
		final String message = (String) scheduleResponse.get("message");
		logger.info("testScheduleCron message = " + message);
		Assert.assertFalse("Message is not null and not blank: " + message,
			message != null && message.trim().length() > 0);
		Assert.assertNotNull("Job key is null", jobKey);
	}

	private static String getCronString(final long time) {
		final Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(time);
		final List<String> args = new ArrayList<>();
		args.add(String.valueOf(cal.get(Calendar.SECOND)));
		args.add(String.valueOf(cal.get(Calendar.MINUTE)));
		args.add(String.valueOf(cal.get(Calendar.HOUR)));
		args.add(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
		args.add(String.valueOf(cal.get(Calendar.MONTH) + 1)); // note: javadocs are wrong for this
		args.add("?"); // day of week
		args.add(String.valueOf(cal.get(Calendar.YEAR)));
		final StringBuilder sb = new StringBuilder();
		String sep = "";
		for (final String arg : args) {
			sb.append(sep);
			sep = " ";
			sb.append(arg);
		}
		return sb.toString();
	}

	@Test
	public void testGetJob() throws Exception {
		final GetJobRequest request = new GetJobRequest();
		request.setSecurityToken(null);
		request.setName("simple-test");
		request.setGroup("simple-test");
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(request);
		logger.info("testJob request = " + requestString);
		final MvcResult statusResult = this.mockMvc.perform(
			get("/job").contentType(MediaType.APPLICATION_JSON).content(requestString).accept(
				MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andDo(document(
					"job",
					requestFields(
						fieldWithPath("securityToken").type(
							JsonFieldType.STRING).optional().description(TOKEN_DESCRIPTION),
						fieldWithPath("name").description(
							JOB_NAME_DESCRIPTION + " that was used to create the schedule"),
						fieldWithPath("group").description(
							JOB_GROUP_DESCRIPTION + " that was used to create the schedule")),
					responseFields(subsectionWithPath("triggers").description("The job triggers"),
						subsectionWithPath("runs").description(
							"Status info of actual runs that have occurred.  "
								+ "This is an object where each key is a report run UUID and the value is the "
								+ "same as what is returned from /status.")))).andReturn();
		final MockHttpServletResponse response2 = statusResult.getResponse();
		Assert.assertTrue(response2.getContentType().startsWith("application/json"));
		final String jsonString2 = response2.getContentAsString();
		logger.info("testJob response = " + jsonString2);
		@SuppressWarnings("unchecked")
		final Map<String, Object> responseMap = mapper.readValue(jsonString2, Map.class);
		logger.info("responseMap = " + responseMap);
	}

	@Test
	public void testGetJobs() throws Exception {
		final BaseRequest request = new BaseRequest();
		request.setSecurityToken(null);
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(request);
		logger.info("testJobs request = " + requestString);
		final MvcResult statusResult = this.mockMvc.perform(
			get("/jobs").contentType(MediaType.APPLICATION_JSON).content(requestString).accept(
				MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andDo(
					document("jobs",
						requestFields(fieldWithPath("securityToken").type(
							JsonFieldType.STRING).optional().description(TOKEN_DESCRIPTION)),
						relaxedResponseFields())).andReturn();
		final MockHttpServletResponse response2 = statusResult.getResponse();
		Assert.assertTrue(response2.getContentType().startsWith("application/json"));
		final String jsonString2 = response2.getContentAsString();
		logger.info("testJobs response = " + jsonString2);
		@SuppressWarnings("unchecked")
		final Map<String, Object> responseMap = mapper.readValue(jsonString2, Map.class);
		logger.info("responseMap = " + responseMap);
	}
}
