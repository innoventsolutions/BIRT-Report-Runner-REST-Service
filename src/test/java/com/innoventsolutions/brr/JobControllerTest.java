/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.brr;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innoventsolutions.brr.entity.BaseRequest;
import com.innoventsolutions.brr.entity.GetJobRequest;
import com.innoventsolutions.brr.entity.StatusRequest;
import com.innoventsolutions.brr.entity.TestRunRequestNoparams;
import com.innoventsolutions.brr.entity.TestRunRequestParams;
import com.innoventsolutions.brr.entity.TestScheduleCronRequest;
import com.innoventsolutions.brr.entity.TestScheduleSimpleRequest;
import com.innoventsolutions.brr.entity.TestSubmitRequestNoparams;
import com.innoventsolutions.brr.entity.WaitforRequest;

import lombok.extern.slf4j.Slf4j;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ReportRunnerApplication.class)
@WebAppConfiguration
@Slf4j
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
	@Rule
	public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-snippets");
	@Autowired
	private WebApplicationContext context;
	private MockMvc mockMvc;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Before
	public void setUp() {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
				.apply(documentationConfiguration(this.restDocumentation)).build();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = df.format(new Date());
		// get full path to report design file (which is in a different package)
		final URL designFileURL = this.getClass().getResource("test.rptdesign");
		final File designFile = new File(designFileURL.getPath());
		System.out.println("****** Inserting test data in the table: Authorization ******");
		String sqlStatements[] = { "truncate table brrs.authorization",
				"insert into brrs.authorization (id, security_token, design_file, submit_time) values(1, 'test-token-report', '"
						+ designFile.getAbsolutePath() + "', '" + dateString + "')",
				"insert into brrs.authorization (id, security_token, design_file, submit_time) values(2, 'test-token-noreport', null, '"
						+ dateString + "')" };

		Arrays.asList(sqlStatements).stream().forEach(sql -> {
			System.out.println(sql);
			jdbcTemplate.execute(sql);
		});
		System.out.println("****** Fetching from table: Authorization ******");
		jdbcTemplate.query("select * from authorization", new RowMapper<Object>() {
			@Override
			public Object mapRow(ResultSet rs, int i) throws SQLException {
				System.out.println(rs.toString());
				/*
				 * System.out.println(String.format("id:%s,token:%s,rpt:%s,time:%s",
				 * rs.getString("id").toString(), rs.getString("securityToken"),
				 * rs.getString("designFile"), rs.getString("submitTime")));
				 */
				return null;
			}
		});
	}

	@Test
	public void testRun() throws Exception {
		final TestRunRequestParams requestObject = new TestRunRequestParams();
		final URL designFileURL = this.getClass().getResource("test.rptdesign");
		requestObject.setDesignFile(designFileURL.getPath());
		requestObject.setFormat("pdf");
		requestObject.setRunThenRender(Boolean.TRUE);

		requestObject.setParameters(new HashMap<>());
		requestObject.getParameters().put("keyFilter", "a.*");
		requestObject.setSecurityToken("test-token-report");
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(requestObject);
		log.info("testRun request = " + requestString);
		this.mockMvc
				.perform(post("/run").contentType(MediaType.APPLICATION_JSON).content(requestString)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(document("run",
						requestFields(fieldWithPath("designFile").description(DESIGN_FILE_DESCRIPTION),
								fieldWithPath("format").optional().description(FORMAT_DESCRIPTION),
								fieldWithPath("runThenRender").optional().description(RUN_THEN_RENDER_DESCRIPTION),
								subsectionWithPath("parameters").optional().type(JsonFieldType.OBJECT)
										.description(PARAMETERS_DESCRIPTION),
								fieldWithPath("securityToken").type(JsonFieldType.STRING).optional()
										.description(TOKEN_DESCRIPTION)),
						responseHeaders(
								headerWithName("Content-Type").description("The content type of the payload"))));
	}

	@Test
	public void testRunNoParams() throws Exception {
		final TestRunRequestNoparams requestObject = new TestRunRequestNoparams();
		final URL designFileURL = this.getClass().getResource("test.rptdesign");
		requestObject.setDesignFile(designFileURL.getPath());
		requestObject.setFormat("pdf");
		requestObject.setRunThenRender(true);
		requestObject.setSecurityToken("test-token-report");
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(requestObject);
		log.info("testRunNoParams request = " + requestString);
		this.mockMvc.perform(post("/run").contentType(MediaType.APPLICATION_JSON).content(requestString)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}

	@Test
	public void testRunRowCount() throws Exception {
		final TestRunRequestParams requestObject = new TestRunRequestParams();
		final URL designFileURL = this.getClass().getResource("test.rptdesign");
		requestObject.setDesignFile(designFileURL.getPath());
		requestObject.setFormat("pdf");
		requestObject.setRunThenRender(true);
		requestObject.setParameters(new HashMap<>());
		requestObject.getParameters().put("rowCount", "5");
		requestObject.setSecurityToken("test-token-report");
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(requestObject);
		log.info("testRunRowCount request = " + requestString);
		this.mockMvc.perform(post("/run").contentType(MediaType.APPLICATION_JSON).content(requestString)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}

	@Test
	public void testSubmit() throws Exception {
		final TestSubmitRequestNoparams requestObject = new TestSubmitRequestNoparams();
		final URL designFileURL = this.getClass().getResource("test.rptdesign");
		requestObject.setDesignFile(designFileURL.getPath());
		requestObject.setFormat("pdf");
		requestObject.setRunThenRender(true);
		requestObject.setNameForHumans("Test Report");
		requestObject.setSecurityToken("test-token-report");
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(requestObject);
		log.info("testSubmit request = " + requestString);
		final MvcResult result = this.mockMvc
				.perform(post("/submit").contentType(MediaType.APPLICATION_JSON).content(requestString)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(document("submit", requestFields(
						fieldWithPath("designFile").description(DESIGN_FILE_DESCRIPTION),
						fieldWithPath("format").optional().description(FORMAT_DESCRIPTION),
						fieldWithPath("runThenRender").optional().description(RUN_THEN_RENDER_DESCRIPTION),
						subsectionWithPath("parameters").optional().type(JsonFieldType.OBJECT)
								.description(PARAMETERS_DESCRIPTION),
						fieldWithPath("securityToken").type(JsonFieldType.STRING).optional()
								.description(TOKEN_DESCRIPTION),
						fieldWithPath("nameForHumans").optional().description(
								"A human friendly name for the report.  This will appear in a report status message and emails."),
						fieldWithPath("sendEmailOnSuccess").type(JsonFieldType.BOOLEAN).optional()
								.description("Indicates whether or not email should be sent for successful generation"),
						fieldWithPath("sendEmailOnFailure").type(JsonFieldType.BOOLEAN).optional()
								.description("Indicates whether or not email should be sent for failed generation"),
						fieldWithPath("mailTo").type(JsonFieldType.STRING).optional()
								.description("Recipient addresses separated by commas"),
						fieldWithPath("mailCc").type(JsonFieldType.STRING).optional()
								.description("Copy-recipient addresses separated by commas"),
						fieldWithPath("mailBcc").type(JsonFieldType.STRING).optional()
								.description("Blind-copy-addresses separated by commas"),
						fieldWithPath("mailSuccessSubject").type(JsonFieldType.STRING).optional()
								.description("Email subject for successful reports"),
						fieldWithPath("mailFailureSubject").type(JsonFieldType.STRING).optional()
								.description("Email subject for failed reports"),
						fieldWithPath("mailSuccessBody").type(JsonFieldType.STRING).optional()
								.description("Email body for successful reports"),
						fieldWithPath("mailFailureBody").type(JsonFieldType.STRING).optional()
								.description("Email body for failed reports")),
						responseFields(
								fieldWithPath("exception")
										.description("Returns the exception object in case of failure"),
								fieldWithPath("uuid").description("Returns the UUID that identifies the job"))))
				.andReturn();
		final MockHttpServletResponse response = result.getResponse();
		Assert.assertTrue(response.getContentType().startsWith("application/json"));
		final String jsonString = response.getContentAsString();
		log.info("testSubmit response = " + jsonString);
		@SuppressWarnings("unchecked")
		final Map<String, String> submitResponse = mapper.readValue(jsonString, Map.class);
		final String jobId = submitResponse.get("uuid");
		log.info("testSubmit jobId = " + jobId);
		final String exceptionString = submitResponse.get("exceptionString");
		log.info("testSubmit exceptionString = " + exceptionString);
		Assert.assertFalse("Exception string is not null and not blank: " + exceptionString,
				exceptionString != null && exceptionString.trim().length() > 0);
		Assert.assertNotNull("Job ID is null", jobId);
	}

	public String submit() throws Exception {
		final TestSubmitRequestNoparams requestObject = new TestSubmitRequestNoparams();
		final URL designFileURL = this.getClass().getResource("test.rptdesign");
		requestObject.setDesignFile(designFileURL.getPath());
		requestObject.setFormat("pdf");
		requestObject.setRunThenRender(true);
		requestObject.setNameForHumans("Test Report");
		requestObject.setSecurityToken("test-token-report");
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(requestObject);
		log.info("submit request = " + requestString);
		final MvcResult result = this.mockMvc.perform(post("/submit").contentType(MediaType.APPLICATION_JSON)
				.content(requestString).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
		final MockHttpServletResponse response = result.getResponse();
		Assert.assertTrue(response.getContentType().startsWith("application/json"));
		final String jsonString = response.getContentAsString();
		log.info("testStatus submit response = " + jsonString);
		@SuppressWarnings("unchecked")
		final Map<String, String> submitResponse = mapper.readValue(jsonString, Map.class);
		final String jobId = submitResponse.get("uuid");
		log.info("testStatus jobId = " + jobId);
		final String exceptionString = submitResponse.get("exceptionString");
		log.info("testStatus exceptionString = " + exceptionString);
		Assert.assertFalse("Exception string is not null and not blank: " + exceptionString,
				exceptionString != null && exceptionString.trim().length() > 0);
		Assert.assertNotNull("Job ID is null", jobId);
		return jobId;
	}

	@Test
	public void testStatus() throws Exception {
		final StatusRequest request = new StatusRequest();
		request.setSecurityToken("test-token-noreport");
		request.setJobId(submit());
		request.setSecurityToken("test-token-report");
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(request);
		log.info("status request = " + requestString);
		final MvcResult statusResult = this.mockMvc
				.perform(get("/status").contentType(MediaType.APPLICATION_JSON).content(requestString)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(document("status",
						requestFields(fieldWithPath("jobId").description(JOB_ID_DESCRIPTION),
								fieldWithPath("securityToken").type(JsonFieldType.STRING).optional()
										.description(TOKEN_DESCRIPTION)),
						responseFields(subsectionWithPath("reportRun").description("The report run request"),
								subsectionWithPath("email").description("The email request"),
								fieldWithPath("startTime").description("The time when the report generation started"),
								fieldWithPath("finishTime").description(
										"The time when report generation and email finished or null if not finished"),
								fieldWithPath("reportFinishTime").description(
										"The time when the report generation finished or null if it is still running"),
								fieldWithPath("duration")
										.description("The number of milliseconds it took to generate the report"),
								fieldWithPath("finished").description("Whether the report generation is finished"),
								subsectionWithPath("errors").description(
										"List of exceptions that were encountered during report generation"),
								subsectionWithPath("emailErrors")
										.description("List of exceptions that were encountered during sending email"))))
				.andReturn();
		final MockHttpServletResponse response2 = statusResult.getResponse();
		Assert.assertTrue(response2.getContentType().startsWith("application/json"));
		final String jsonString2 = response2.getContentAsString();
		log.info("testStatus response = " + jsonString2);
		@SuppressWarnings("unchecked")
		final Map<String, Object> responseMap = mapper.readValue(jsonString2, Map.class);
		log.info("responseMap = " + responseMap);
		@SuppressWarnings("unchecked")
		final List<Object> reportErrors = (List<Object>) responseMap.get("errors");
		Assert.assertNotNull("reportErrors should not be null", reportErrors);
		Assert.assertTrue("reportErrors should be empty", reportErrors.isEmpty());
		@SuppressWarnings("unchecked")
		final Map<String, Object> emailErrors = (Map<String, Object>) responseMap.get("emailErrors");
		Assert.assertNotNull("emailErrors should not be null", emailErrors);
		Assert.assertTrue("emailErrors should be empty", emailErrors.isEmpty());
	}

	@Test
	public void testWaitFor() throws Exception {
		final WaitforRequest request = new WaitforRequest();
		request.setSecurityToken("test-token-noreport");
		request.setJobId(submit());
		request.setTimeout(Long.valueOf(5000L));
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(request);
		log.info("status request = " + requestString);
		final MvcResult statusResult = this.mockMvc
				.perform(get("/waitfor").contentType(MediaType.APPLICATION_JSON).content(requestString)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(document("waitfor",
						requestFields(fieldWithPath("jobId").description(JOB_ID_DESCRIPTION), fieldWithPath(
								"securityToken").type(JsonFieldType.STRING).optional().description(TOKEN_DESCRIPTION),
								fieldWithPath("timeout").description("The maximum number of milliseconds to wait")),
						responseFields(subsectionWithPath("reportRun").description("The report run request"),
								subsectionWithPath("email").description("The email request"),
								fieldWithPath("startTime").description("The time when the report generation started"),
								fieldWithPath("finishTime").description(
										"The time when report generation and email finished or null if not finished"),
								fieldWithPath("reportFinishTime").description(
										"The time when the report generation finished or null if it is still running"),
								fieldWithPath("duration")
										.description("The number of milliseconds it took to generate the report"),
								fieldWithPath("finished").description("Whether the report generation is finished"),
								subsectionWithPath("errors").description(
										"List of exceptions that were encountered during report generation"),
								subsectionWithPath("emailErrors")
										.description("List of exceptions that were encountered during sending email"))))
				.andReturn();
		final MockHttpServletResponse response2 = statusResult.getResponse();
		Assert.assertTrue(response2.getContentType().startsWith("application/json"));
		final String jsonString2 = response2.getContentAsString();
		log.info("testStatus response = " + jsonString2);
		@SuppressWarnings("unchecked")
		final Map<String, Object> responseMap = mapper.readValue(jsonString2, Map.class);
		log.info("responseMap = " + responseMap);
		@SuppressWarnings("unchecked")
		final List<Object> reportErrors = (List<Object>) responseMap.get("errors");
		Assert.assertNotNull("reportErrors should not be null", reportErrors);
		Assert.assertTrue("reportErrors should be empty", reportErrors.isEmpty());
		@SuppressWarnings("unchecked")
		final Map<String, Object> emailErrors = (Map<String, Object>) responseMap.get("emailErrors");
		Assert.assertNotNull("emailErrors should not be null", emailErrors);
		Assert.assertTrue("emailErrors should be empty", emailErrors.isEmpty());
	}

	@Test
	public void testStatusAll() throws Exception {
		final BaseRequest request = new BaseRequest();
		request.setSecurityToken("test-token-noreport");
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(request);
		log.info("status-all request = " + requestString);
		final MvcResult statusResult = this.mockMvc
				.perform(get("/status-all").contentType(MediaType.APPLICATION_JSON).content(requestString)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(
						status().isOk())
				.andDo(document("status-all", requestFields(fieldWithPath("securityToken").type(JsonFieldType.STRING)
						.optional().description(TOKEN_DESCRIPTION)), relaxedResponseFields()))
				.andReturn();
		final MockHttpServletResponse response2 = statusResult.getResponse();
		Assert.assertTrue(response2.getContentType().startsWith("application/json"));
		final String jsonString2 = response2.getContentAsString();
		log.info("testStatusAll response = " + jsonString2);
		@SuppressWarnings("unchecked")
		final Map<String, Object> responseMap = mapper.readValue(jsonString2, Map.class);
		log.info("responseMap = " + responseMap);
	}

	@Test
	public void testGet() throws Exception {
		final StatusRequest request = new StatusRequest();
		request.setSecurityToken("test-token-noreport");
		request.setJobId(submit());
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(request);
		log.info("get request = " + requestString);
		this.mockMvc
				.perform(get("/get").contentType(MediaType.APPLICATION_JSON).content(requestString)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(document("get",
						requestFields(fieldWithPath("jobId").description(JOB_ID_DESCRIPTION),
								fieldWithPath("securityToken").type(JsonFieldType.STRING).optional()
										.description(TOKEN_DESCRIPTION)),
						responseHeaders(headerWithName("Content-Type").description("The content type of the payload"))))
				.andReturn();
	}

	@Test
	public void testDownload() throws Exception {
		final StatusRequest request = new StatusRequest();
		request.setSecurityToken("test-token-noreport");
		request.setJobId(submit());
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(request);
		log.info("get request = " + requestString);
		this.mockMvc
				.perform(get("/download").contentType(MediaType.APPLICATION_JSON).content(requestString)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(document("download",
						requestFields(fieldWithPath("jobId").description(JOB_ID_DESCRIPTION),
								fieldWithPath("securityToken").type(JsonFieldType.STRING).optional()
										.description(TOKEN_DESCRIPTION)),
						responseHeaders(headerWithName("Content-Type").description("The content type of the payload"))))
				.andReturn();
	}

	//TODO Fix Test
	@Test
	public void testScheduleSimple() throws Exception {
		final TestScheduleSimpleRequest requestObject = new TestScheduleSimpleRequest();
		final URL designFileURL = this.getClass().getResource("test.rptdesign");
		requestObject.setSubmit(new TestSubmitRequestNoparams());
		requestObject.getSubmit().setDesignFile(designFileURL.getPath());
		requestObject.getSubmit().setFormat("PDF");
		requestObject.getSubmit().setRunThenRender(true);
		requestObject.getSubmit().setNameForHumans("Test Report");
		requestObject.setGroup("simple-test");
		requestObject.setName("simple-test");
		requestObject.setStartDate(null);
		requestObject.setIntervalInMilliseconds(Long.valueOf(1000L));
		requestObject.setRepeatCount(1);
		requestObject.setMisfireInstruction(null);
		requestObject.setSecurityToken("test-token-report");
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(requestObject);
		log.info("testScheduleSimple request = " + requestString);
		final MvcResult result = this.mockMvc
				.perform(post("/schedule-simple").contentType(MediaType.APPLICATION_JSON).content(requestString)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(document("schedule-simple", requestFields(
						fieldWithPath("group").description(JOB_GROUP_DESCRIPTION),
						fieldWithPath("name").description(JOB_NAME_DESCRIPTION),
						fieldWithPath("startDate").description(JOB_START_DATE_DESCRIPTION),
						fieldWithPath("intervalInMilliseconds").optional()
								.description("The interval in milliseconds between runs"),
						fieldWithPath("repeatCount").optional().description(
								"The number of times to repeat the run. The default is to repeat forever."),
						fieldWithPath("misfireInstruction").optional().description(
								"One of 'ignore', 'fire-now', 'next-with-existing-count', 'next-with-remaining-count', 'now-with-existing-count', or 'now-with-remaining-count'"),
						fieldWithPath("securityToken").type(JsonFieldType.STRING).optional()
								.description(TOKEN_DESCRIPTION),
						fieldWithPath("submit.designFile").description(DESIGN_FILE_DESCRIPTION),
						fieldWithPath("submit.format").optional().description(FORMAT_DESCRIPTION),
						fieldWithPath("submit.runThenRender").optional().description(RUN_THEN_RENDER_DESCRIPTION),
						subsectionWithPath("submit.parameters").optional().type(JsonFieldType.OBJECT)
								.description(PARAMETERS_DESCRIPTION),
						fieldWithPath("submit.nameForHumans").optional().description(
								"A human friendly name for the report.  This will appear in a report status message and emails."),
						fieldWithPath("submit.sendEmailOnSuccess").type(JsonFieldType.BOOLEAN).optional()
								.description("Indicates whether or not email should be sent for successful generation"),
						fieldWithPath("submit.sendEmailOnFailure").type(JsonFieldType.BOOLEAN).optional()
								.description("Indicates whether or not email should be sent for failed generation"),
						fieldWithPath("submit.mailTo").type(JsonFieldType.STRING).optional()
								.description("Recipient addresses separated by commas"),
						fieldWithPath("submit.mailCc").type(JsonFieldType.STRING).optional()
								.description("Copy-recipient addresses separated by commas"),
						fieldWithPath("submit.mailBcc").type(JsonFieldType.STRING).optional()
								.description("Blind-copy-addresses separated by commas"),
						fieldWithPath("submit.mailSuccessSubject").type(JsonFieldType.STRING).optional()
								.description("Email subject for successful reports"),
						fieldWithPath("submit.mailFailureSubject").type(JsonFieldType.STRING).optional()
								.description("Email subject for failed reports"),
						fieldWithPath("submit.mailSuccessBody").type(JsonFieldType.STRING).optional()
								.description("Email body for successful reports"),
						fieldWithPath("submit.mailFailureBody").type(JsonFieldType.STRING).optional()
								.description("Email body for failed reports"),
						fieldWithPath("submit.securityToken").type(JsonFieldType.STRING).optional()
								.description(TOKEN_DESCRIPTION)),
						responseFields(
								fieldWithPath("message")
										.description("Returns the exception message string in case of failure"),
								fieldWithPath("jobKey.group").description("The job group name passed in the request"),
								fieldWithPath("jobKey.name").description("The job name passed in the request"))))
				.andReturn();
		final MockHttpServletResponse response = result.getResponse();
		Assert.assertTrue(response.getContentType().startsWith("application/json"));
		final String jsonString = response.getContentAsString();
		log.info("testScheduleSimple response = " + jsonString);
		@SuppressWarnings("unchecked")
		final Map<String, Object> scheduleResponse = mapper.readValue(jsonString, Map.class);
		@SuppressWarnings("unchecked")
		final Map<String, String> jobKey = (Map<String, String>) scheduleResponse.get("jobKey");
		log.info("testScheduleSimple jobKey = " + jobKey);
		final String message = (String) scheduleResponse.get("message");
		log.info("testScheduleSimple message = " + message);
		Assert.assertFalse("Message is not null and not blank: " + message,
				message != null && message.trim().length() > 0);
		Assert.assertNotNull("Job key is null", jobKey);
	}

	//TODO Fix Test
	@Test
	public void testScheduleCron() throws Exception {
		final TestScheduleCronRequest requestObject = new TestScheduleCronRequest();
		final URL designFileURL = this.getClass().getResource("test.rptdesign");
		requestObject.setSubmit(new TestSubmitRequestNoparams());
		requestObject.getSubmit().setDesignFile( designFileURL.getPath());
		requestObject.getSubmit().setFormat( "PDF");
		requestObject.getSubmit().setRunThenRender( true);
		requestObject.getSubmit().setNameForHumans( "Test Report");
		requestObject.setGroup( "cron-test");
		requestObject.setName( "cron-test");
		requestObject.setStartDate( null);
		requestObject.setSecurityToken("test-token-report");
		final long time = System.currentTimeMillis() + 31L * 24L * 60L * 60L * 1000L;
		log.info("cron time = " + new Date(time));
		requestObject.setCronString( getCronString(time));
		requestObject.setMisfireInstruction( null);
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(requestObject);
		log.info("testScheduleCron request = " + requestString);
		final MvcResult result = this.mockMvc
				.perform(post("/schedule-cron").contentType(MediaType.APPLICATION_JSON).content(requestString)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(document("schedule-cron", requestFields(
						fieldWithPath("group").description(JOB_GROUP_DESCRIPTION),
						fieldWithPath("name").description(JOB_NAME_DESCRIPTION),
						fieldWithPath("startDate").description(JOB_START_DATE_DESCRIPTION),
						fieldWithPath("cronString").description("The cron string as described in cron documentation"),
						fieldWithPath("misfireInstruction").optional()
								.description("One of 'ignore', 'fire-and-proceed', or 'do-nothing'"),
						fieldWithPath("securityToken").type(JsonFieldType.STRING).optional()
								.description(TOKEN_DESCRIPTION),
						fieldWithPath("submit.designFile").description(DESIGN_FILE_DESCRIPTION),
						fieldWithPath("submit.format").optional().description(FORMAT_DESCRIPTION),
						fieldWithPath("submit.runThenRender").optional().description(RUN_THEN_RENDER_DESCRIPTION),
						subsectionWithPath("submit.parameters").optional().type(JsonFieldType.OBJECT)
								.description(PARAMETERS_DESCRIPTION),
						fieldWithPath("submit.nameForHumans").optional().description(
								"A human friendly name for the report.  This will appear in a report status message and emails."),
						fieldWithPath("submit.sendEmailOnSuccess").type(JsonFieldType.BOOLEAN).optional()
								.description("Indicates whether or not email should be sent for successful generation"),
						fieldWithPath("submit.sendEmailOnFailure").type(JsonFieldType.BOOLEAN).optional()
								.description("Indicates whether or not email should be sent for failed generation"),
						fieldWithPath("submit.mailTo").type(JsonFieldType.STRING).optional()
								.description("Recipient addresses separated by commas"),
						fieldWithPath("submit.mailCc").type(JsonFieldType.STRING).optional()
								.description("Copy-recipient addresses separated by commas"),
						fieldWithPath("submit.mailBcc").type(JsonFieldType.STRING).optional()
								.description("Blind-copy-addresses separated by commas"),
						fieldWithPath("submit.mailSuccessSubject").type(JsonFieldType.STRING).optional()
								.description("Email subject for successful reports"),
						fieldWithPath("submit.mailFailureSubject").type(JsonFieldType.STRING).optional()
								.description("Email subject for failed reports"),
						fieldWithPath("submit.mailSuccessBody").type(JsonFieldType.STRING).optional()
								.description("Email body for successful reports"),
						fieldWithPath("submit.mailFailureBody").type(JsonFieldType.STRING).optional()
								.description("Email body for failed reports"),
						fieldWithPath("submit.securityToken").type(JsonFieldType.STRING).optional()
								.description(TOKEN_DESCRIPTION)),
						responseFields(
								fieldWithPath("message")
										.description("Returns the exception message string in case of failure"),
								fieldWithPath("jobKey.group").description("The job group name passed in the request"),
								fieldWithPath("jobKey.name").description("The job name passed in the request"))))
				.andReturn();
		final MockHttpServletResponse response = result.getResponse();
		Assert.assertTrue(response.getContentType().startsWith("application/json"));
		final String jsonString = response.getContentAsString();
		log.info("testScheduleCron response = " + jsonString);
		@SuppressWarnings("unchecked")
		final Map<String, Object> scheduleResponse = mapper.readValue(jsonString, Map.class);
		@SuppressWarnings("unchecked")
		final Map<String, String> jobKey = (Map<String, String>) scheduleResponse.get("jobKey");
		log.info("testScheduleCron jobKey = " + jobKey);
		final String message = (String) scheduleResponse.get("message");
		log.info("testScheduleCron message = " + message);
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
		request.setSecurityToken("test-token-noreport");
		request.setName("simple-test");
		request.setGroup("simple-test");
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(request);
		log.info("testJob request = " + requestString);
		final MvcResult statusResult = this.mockMvc
				.perform(get("/job").contentType(MediaType.APPLICATION_JSON).content(requestString)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(document("job",
						requestFields(fieldWithPath("securityToken").type(JsonFieldType.STRING).optional()
								.description(TOKEN_DESCRIPTION),
								fieldWithPath("name").description(JOB_NAME_DESCRIPTION
										+ " that was used to create the schedule"),
								fieldWithPath("group")
										.description(JOB_GROUP_DESCRIPTION + " that was used to create the schedule")),
						responseFields(subsectionWithPath("triggers").description("The job triggers"),
								subsectionWithPath("runs")
										.description("Status info of actual runs that have occurred.  "
												+ "This is an object where each key is a report run UUID and the value is the "
												+ "same as what is returned from /status."))))
				.andReturn();
		final MockHttpServletResponse response2 = statusResult.getResponse();
		Assert.assertTrue(response2.getContentType().startsWith("application/json"));
		final String jsonString2 = response2.getContentAsString();
		log.info("testJob response = " + jsonString2);
		@SuppressWarnings("unchecked")
		final Map<String, Object> responseMap = mapper.readValue(jsonString2, Map.class);
		log.info("responseMap = " + responseMap);
	}

	@Test
	public void testGetJobs() throws Exception {
		final BaseRequest request = new BaseRequest();
		request.setSecurityToken("test-token-noreport");
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(request);
		log.info("testJobs request = " + requestString);
		final MvcResult statusResult = this.mockMvc
				.perform(get("/jobs").contentType(MediaType.APPLICATION_JSON).content(requestString)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(
						status().isOk())
				.andDo(document("jobs", requestFields(fieldWithPath("securityToken").type(JsonFieldType.STRING)
						.optional().description(TOKEN_DESCRIPTION)), relaxedResponseFields()))
				.andReturn();
		final MockHttpServletResponse response2 = statusResult.getResponse();
		Assert.assertTrue(response2.getContentType().startsWith("application/json"));
		final String jsonString2 = response2.getContentAsString();
		log.info("testJobs response = " + jsonString2);
		@SuppressWarnings("unchecked")
		final Map<String, Object> responseMap = mapper.readValue(jsonString2, Map.class);
		log.info("responseMap = " + responseMap);
	}

	@Test
	public void testDeleteJob() throws Exception {
		final GetJobRequest request = new GetJobRequest();
		request.setSecurityToken("test-token-noreport");
		request.setName("simple-test");
		request.setGroup("simple-test");
		final ObjectMapper mapper = new ObjectMapper();
		final String requestString = mapper.writeValueAsString(request);
		log.info("testDeleteJob request = " + requestString);
		final MvcResult statusResult = this.mockMvc
				.perform(delete("/job").contentType(MediaType.APPLICATION_JSON).content(requestString)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(document("delete-job",
						requestFields(
								fieldWithPath("securityToken").type(JsonFieldType.STRING).optional()
										.description(TOKEN_DESCRIPTION),
								fieldWithPath("name")
										.description(JOB_NAME_DESCRIPTION + " that was used to create the schedule"),
								fieldWithPath("group")
										.description(JOB_GROUP_DESCRIPTION + " that was used to create the schedule")),
						responseFields(fieldWithPath("jobDeleted").description("True if the job was deleted"))))
				.andReturn();
		final MockHttpServletResponse response2 = statusResult.getResponse();
		Assert.assertTrue(response2.getContentType().startsWith("application/json"));
		final String jsonString2 = response2.getContentAsString();
		log.info("testDeleteJob response = " + jsonString2);
		final Object response = mapper.readValue(jsonString2, Object.class);
		log.info("response = " + response);
	}
}
