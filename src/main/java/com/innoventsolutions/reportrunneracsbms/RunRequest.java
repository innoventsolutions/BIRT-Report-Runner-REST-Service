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

import java.util.HashMap;
import java.util.Map;

public class RunRequest {
	private String designFile;
	private String format;
	private boolean runThenRender;
	private Map<String, Object> parameters = new HashMap<>();

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

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(final Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	public boolean isRunThenRender() {
		return runThenRender;
	}

	public void setRunThenRender(final boolean runThenRender) {
		this.runThenRender = runThenRender;
	}
}
