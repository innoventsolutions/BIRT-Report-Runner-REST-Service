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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class InitServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static RunnerContext runnerContext = null;

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		resp.setContentType("text/html");
		Map<UUID, ReportRunStatus> map;
		synchronized (runnerContext.reports) {
			map = new HashMap<>(runnerContext.reports);
		}
		final PrintWriter writer = resp.getWriter();
		writer.print("<html>\n");
		writer.print("<body>\n");
		writer.print("<table>\n");
		writer.print("<tr>");
		writer.print("<th>ID</th>");
		writer.print("<th>Started</th>");
		writer.print("<th>Finished</th>");
		writer.print("</tr>\n");
		for (final UUID uuid : map.keySet()) {
			final ReportRunStatus status = map.get(uuid);
			writer.print("<tr>");
			writer.print("<td>");
			writer.print(uuid);
			writer.print("</td>");
			writer.print("<td>");
			writer.print(status.startTime);
			writer.print("</td>");
			writer.print("<td>");
			final Date finishTime = status.getFinishTime();
			if (finishTime != null) {
				writer.print(status.getFinishTime());
			}
			writer.print("</td>");
			writer.print("</tr>\n");
			final List<Exception> exceptions = status.getErrors();
			if (exceptions != null) {
				for (final Exception exception : exceptions) {
					writer.print("<tr>");
					writer.print("<td colspan=\"3\" style=\"padding-left: 0.3in\">");
					writer.print(exception);
					writer.print("</td>");
					writer.print("</tr>\n");
				}
			}
		}
		writer.print("</table>\n");
		writer.print("</body>\n");
		writer.print("</html>\n");
	}

	@Override
	public void init(final ServletConfig config) throws ServletException {
		final String filename = config.getInitParameter("properties");
		try {
			runnerContext = new RunnerContext(filename);
		}
		catch (final Exception e) {
			throw new ServletException("Unable to read properties file " + filename, e);
		}
	}
}
