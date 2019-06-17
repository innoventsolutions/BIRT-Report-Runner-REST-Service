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

import java.util.HashMap;
import java.util.Map;

/**
 * The immutable report run request.
 *
 * @author Steve Schafer, Innovent Solutions Inc.
 */
public class ReportRun {
	public final String designFile;
	public final String nameForHumans;
	public final String format;
	public final String outputFile;
	public final boolean runThenRender;
	public final Map<String, Object> parameters;
	public final String securityToken;

	public ReportRun(final String designFile, final String nameForHumans, final String format,
			final String outputFile, final boolean runThenRender,
			final Map<String, Object> parameters, final String securityToken) {
		this.nameForHumans = nameForHumans;
		if (designFile == null) {
			throw new NullPointerException("designFile may not be null");
		}
		this.designFile = designFile;
		if (format == null) {
			throw new NullPointerException("format may not be null");
		}
		this.format = format;
		if (outputFile == null) {
			throw new NullPointerException("outputFile may not be null");
		}
		this.outputFile = outputFile;
		this.runThenRender = runThenRender;
		if (parameters == null) {
			throw new NullPointerException("parameters may not be null");
		}
		this.parameters = new HashMap<>(parameters);
		this.securityToken = securityToken;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("ReportRun");
		sb.append(", designFile = ");
		sb.append(designFile);
		sb.append(", format = ");
		sb.append(format);
		sb.append(", outputFile = ");
		sb.append(outputFile);
		if (runThenRender) {
			sb.append(", runThenRender");
		}
		sb.append(", parameters = ");
		sb.append(parameters);
		return sb.toString();
	}
}
