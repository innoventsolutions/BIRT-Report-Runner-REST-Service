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

import java.io.File;
import java.util.Properties;

public class PropertiesHelper {
	private final Properties properties;

	public PropertiesHelper(final Properties properties) {
		this.properties = properties;
	}

	public String get(final String key, final String defaultValue) {
		final String value = properties.getProperty(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	public String requireString(final String key) {
		final String value = properties.getProperty(key);
		if (value == null) {
			throw new IllegalArgumentException("Property " + key + " is required");
		}
		return value;
	}

	public int get(final String key, final int defaultValue) {
		final String value = properties.getProperty(key);
		if (value == null) {
			return defaultValue;
		}
		return Integer.parseInt(value);
	}

	public int requireInteger(final String key) {
		final String value = properties.getProperty(key);
		if (value == null) {
			throw new IllegalArgumentException("Property " + key + " is required");
		}
		return Integer.parseInt(value);
	}

	public boolean get(final String key, final boolean defaultValue) {
		final String value = properties.getProperty(key);
		if (value == null) {
			return defaultValue;
		}
		return Boolean.parseBoolean(value);
	}

	public boolean requireBoolean(final String key) {
		final String value = properties.getProperty(key);
		if (value == null) {
			throw new IllegalArgumentException("Property " + key + " is required");
		}
		return Boolean.parseBoolean(value);
	}

	public File get(final String key, final File defaultValue, final File parentDir) {
		final String value = properties.getProperty(key);
		if (value == null) {
			return defaultValue;
		}
		final File file = new File(value);
		if (file.isAbsolute()) {
			return file;
		}
		return new File(parentDir, value);
	}

	public File requireFile(final String key, final File parentDir) {
		final String value = properties.getProperty(key);
		if (value == null) {
			throw new IllegalArgumentException("Property " + key + " is required");
		}
		final File file = new File(value);
		if (file.isAbsolute()) {
			return file;
		}
		return new File(parentDir, value);
	}
}
