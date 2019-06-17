package com.innoventsolutions.reportrunneracsbms;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
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

	@Test
	public void testRun() throws Exception {
		JobController.initializeRunnerContextFromResource(
			this.getClass().getResource("report-runner.properties"));
		final String requestString = getResourceAsString("unit-test-request.json");
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

	@Test
	public void testRunNoParams() throws Exception {
		JobController.initializeRunnerContextFromResource(
			this.getClass().getResource("report-runner.properties"));
		final String requestString = getResourceAsString("unit-test-request-noparams.json");
		logger.info("testRunNoParams request = " + requestString);
		this.mockMvc.perform(
			post("/run").contentType(MediaType.APPLICATION_JSON).content(requestString).accept(
				MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}

	@Test
	public void testRunRowCount() throws Exception {
		JobController.initializeRunnerContextFromResource(
			this.getClass().getResource("report-runner.properties"));
		final String requestString = getResourceAsString("unit-test-request-rowcount.json");
		logger.info("testRunRowCount request = " + requestString);
		this.mockMvc.perform(
			post("/run").contentType(MediaType.APPLICATION_JSON).content(requestString).accept(
				MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}

	@Test
	public void testSubmit() throws Exception {
		JobController.initializeRunnerContextFromResource(
			this.getClass().getResource("report-runner.properties"));
		final String requestString = getResourceAsString("unit-test-submit-request.json");
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
		final ObjectMapper mapper = new ObjectMapper();
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
		JobController.initializeRunnerContextFromResource(
			this.getClass().getResource("report-runner.properties"));
		final String requestString = getResourceAsString("unit-test-submit-request.json");
		logger.info("testStatus request = " + requestString);
		final MvcResult result = this.mockMvc.perform(
			post("/submit").contentType(MediaType.APPLICATION_JSON).content(requestString).accept(
				MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();
		final MockHttpServletResponse response = result.getResponse();
		Assert.assertTrue(response.getContentType().startsWith("application/json"));
		final String jsonString = response.getContentAsString();
		logger.info("testStatus submit response = " + jsonString);
		final ObjectMapper mapper = new ObjectMapper();
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

	private Map<String, Object> testStatus(final String op) throws Exception {
		final String jobId = submit();
		final MvcResult statusResult = this.mockMvc.perform(get("/" + op + "/{uuid}", jobId).accept(
			MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andDo(document(
				op,
				pathParameters(
					parameterWithName("uuid").description("The identifier returned from submit")),
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
		final ObjectMapper mapper = new ObjectMapper();
		@SuppressWarnings("unchecked")
		final Map<String, Object> responseMap = mapper.readValue(jsonString2, Map.class);
		logger.info("responseMap = " + responseMap);
		return responseMap;
	}

	@Test
	public void testStatus() throws Exception {
		testStatus("status");
	}

	@Test
	public void testWaitFor() throws Exception {
		final Map<String, Object> responseMap = testStatus("waitfor");
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
	public void testGet() throws Exception {
		final String jobId = submit();
		this.mockMvc.perform(
			get("/get/{uuid}", jobId).accept(MediaType.APPLICATION_JSON)).andExpect(
				status().isOk()).andDo(
					document("get",
						pathParameters(parameterWithName("uuid").description(
							"The identifier returned from submit")),
						responseHeaders(headerWithName("Content-Type").description(
							"The content type of the payload")))).andReturn();
	}

	private static String getResourceAsString(final String name) throws IOException {
		final URL requestURL = JobControllerTest.class.getResource(name);
		if (requestURL == null) {
			Assert.fail(name + " not found in classpath");
		}
		// get the resource into a string
		final BufferedInputStream bis = (BufferedInputStream) requestURL.getContent();
		final Reader r = new InputStreamReader(bis);
		final BufferedReader br = new BufferedReader(r);
		final StringBuilder sb = new StringBuilder();
		final char[] buffer = new char[0x1000];
		int charsRead = br.read(buffer);
		while (charsRead >= 0) {
			sb.append(buffer, 0, charsRead);
			charsRead = br.read(buffer);
		}
		return sb.toString();
	}
}
