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

import javax.sql.DataSource;

import org.apache.catalina.Context;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ReportRunnerAcSbmsApplication {
	public static void main(final String[] args) {
		SpringApplication.run(ReportRunnerAcSbmsApplication.class, args);
	}

	/**
	 * Servlet test
	 */
	@Bean
	public ServletRegistrationBean<HelloServlet> helloServletRegistrationBean() {
		final ServletRegistrationBean<HelloServlet> result = new ServletRegistrationBean<>(
				new HelloServlet(), "/hello"); // Comma separated url paths.
		result.setName("helloServlet");
		result.setOrder(1);
		return result;
	}

	/**
	 * for JNDI
	 */
	@Bean
	public TomcatServletWebServerFactory tomcatFactory() {
		return new MyTomcatServletWebServerFactory();
	}

	static class MyTomcatServletWebServerFactory extends TomcatServletWebServerFactory {
		@Autowired
		ConfigService config;

		@Override
		protected TomcatWebServer getTomcatWebServer(
				final org.apache.catalina.startup.Tomcat tomcat) {
			tomcat.enableNaming();
			return super.getTomcatWebServer(tomcat);
		}

		@Override
		protected void postProcessContext(final Context context) {
			// context
			final ContextResource resource = new ContextResource();
			resource.setName("jdbc/reportRunnerSecurity");
			resource.setType(DataSource.class.getName());
			resource.setProperty("driverClassName", config.dbDriver);
			resource.setProperty("url", config.dbUrl);
			resource.setProperty("username", config.dbUsername);
			resource.setProperty("password", config.dbPassword);
			context.getNamingResources().addResource(resource);
		}
	}
}
