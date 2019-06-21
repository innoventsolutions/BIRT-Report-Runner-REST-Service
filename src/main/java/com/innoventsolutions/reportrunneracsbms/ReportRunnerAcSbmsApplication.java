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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ReportRunnerAcSbmsApplication {
	public static void main(final String[] args) {
		SpringApplication.run(ReportRunnerAcSbmsApplication.class, args);
	}

	@Bean
	public ServletRegistrationBean<HelloServlet> helloServletRegistrationBean() {
		final ServletRegistrationBean<HelloServlet> result = new ServletRegistrationBean<>(
				new HelloServlet(), "/hello"); // Comma separated url paths.
		result.setName("helloServlet");
		result.setOrder(1);
		return result;
	}
}
