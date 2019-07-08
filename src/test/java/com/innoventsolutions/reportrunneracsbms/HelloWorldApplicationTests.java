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

import static org.assertj.core.api.BDDAssertions.then;

import java.util.Map;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

// @RunWith(SpringRunner.class)
// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// @TestPropertySource(properties = { "management.port=0" })
public class HelloWorldApplicationTests {
	// @LocalServerPort
	private int port;
	// @Value("${local.management.port}")
	private int mgt;
	// @Autowired
	private TestRestTemplate testRestTemplate;

	// @Test
	public void shouldReturn200WhenSendingRequestToController() throws Exception {
		@SuppressWarnings("rawtypes")
		final ResponseEntity<Map> entity = this.testRestTemplate.getForEntity(
			"http://localhost:" + this.port + "/hello-world", Map.class);
		then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	// @Test
	public void shouldReturn200WhenSendingRequestToManagementEndpoint() throws Exception {
		@SuppressWarnings("rawtypes")
		final ResponseEntity<Map> entity = this.testRestTemplate.getForEntity(
			"http://localhost:" + this.mgt + "/actuator/info", Map.class);
		then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
}
