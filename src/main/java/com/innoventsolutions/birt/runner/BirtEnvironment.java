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
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.eclipse.birt.core.exception.BirtException;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineConstants;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.model.api.DesignConfig;
import org.eclipse.birt.report.model.api.IDesignEngine;
import org.eclipse.birt.report.model.api.IDesignEngineFactory;

/**
 * @author Steve Schafer, Innovent Solutions Inc.
 * @deprecated Use BirtService instead.
 */
@Deprecated
public class BirtEnvironment {
	// private static final Logger logger = Logger.getLogger( BirtEnvironment.class.getName() );
	final File workspace;
	final File birtRuntimeHome;
	final File resourcePath;
	final File scriptLib;
	final File outputDir;
	final String reportFormat;
	final String baseImageURL;
	final File loggingDir;
	final File loggingPropertiesFile;
	final boolean isActuate;

	public BirtEnvironment(final Configuration configuration) {
		outputDir = configuration.outputDirectory;
		// default directory for report design files
		workspace = configuration.workspace;
		// Eclipse installation directory, parent of plugins
		birtRuntimeHome = configuration.birtRuntimeHome;
		// Where report resources live
		if (configuration.resourcePath == null) {
			throw new NullPointerException("Resource path is missing");
		}
		resourcePath = configuration.resourcePath;
		// Where event handler jar files live
		scriptLib = configuration.scriptLib;
		// Default report format
		reportFormat = configuration.reportFormat;
		// Location for images
		baseImageURL = configuration.baseImageURL;
		loggingDir = configuration.loggingDir;
		loggingPropertiesFile = configuration.loggingPropertiesFile;
		isActuate = configuration.isActuate;
	}

	public String logValues() {
		final StringBuilder sb = new StringBuilder();
		sb.append("\n\tWORKSPACE: ");
		sb.append(workspace == null ? "null" : workspace.getAbsolutePath());
		sb.append("\n\tBIRT_RUNTIME_HOME: ");
		sb.append(birtRuntimeHome == null ? "null" : birtRuntimeHome.getAbsolutePath());
		sb.append("\n\tRESOURCE_PATH: ");
		sb.append(resourcePath == null ? "null" : resourcePath.getAbsolutePath());
		sb.append("\n\tSCRIPT_LIB: ");
		sb.append(scriptLib == null ? "null" : scriptLib.getAbsolutePath());
		return sb.toString();
	}

	public IReportEngine getReportEngine() throws IOException, BirtException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		final EngineConfig config = new EngineConfig();
		System.out.println("birtRuntimeHome = " + birtRuntimeHome);
		if (birtRuntimeHome != null) {
			final String birtHome = birtRuntimeHome.getAbsolutePath();
			if (isActuate) {
				config.setBIRTHome(birtHome);
			}
			else {
				config.setEngineHome(birtHome);
			}
		}
		if (resourcePath != null) {
			final String resourceDir = resourcePath.getAbsolutePath();
			config.setResourcePath(resourceDir);
		}
		final String scriptlibFileNames = getScriptLibFileNames();
		if (scriptlibFileNames != null) {
			config.setProperty(EngineConstants.WEBAPP_CLASSPATH_KEY, scriptlibFileNames);
		}
		final File loggingProperties = loggingPropertiesFile == null
			? new File("./logging.properties")
			: loggingPropertiesFile;
		LogManager.getLogManager().readConfiguration(new FileInputStream(loggingProperties));
		final File logDir = new File("./log");
		if (!logDir.exists()) {
			logDir.mkdir();
		}
		final Logger rootLogger = Logger.getLogger("");
		final Handler[] handlers = rootLogger.getHandlers();
		for (final Handler handler : handlers) {
			handler.setFormatter(new BatchFormatter());
		}
		// control debug of BIRT components.
		final File loggingDirFile = loggingDir == null ? new File("./log") : loggingDir;
		if (!loggingDirFile.exists()) {
			loggingDirFile.mkdirs();
		}
		config.setLogConfig(loggingDirFile.getAbsolutePath(), Level.WARNING);
		return isActuate ? getActuateReportEngine(config) : getReportEngine(config);
	}

	public static IReportEngine getReportEngine(final EngineConfig config) throws BirtException {
		System.out.println("before Platform startup");
		Platform.startup(config);
		System.out.println("after Platform startup");
		final IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject(
			IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
		if (factory == null) {
			System.out.println("Could not create report engine factory");
			throw new NullPointerException("Could not create report engine factory");
		}
		final IReportEngine engine = factory.createReportEngine(config);
		if (engine == null) {
			System.out.println("Could not create report engine");
			throw new NullPointerException("Could not create report engine");
		}
		return engine;
	}

	public static IReportEngine getActuateReportEngine(final EngineConfig config)
			throws BirtException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		Platform.startup(config);
		final Object factoryObjectForReflection = Platform.createFactoryObject(
			"com.actuate.birt.report.engine.ActuateReportEngineFactory" /* IActuateReportEngineFactory.EXTENSION_ACTUATE_REPORT_ENGINE_FACTORY */ );
		// when using the Actuate Report Engine Factory, the return type is
		// not exposed publicly, so you cannot instantiate the factory
		// under normal conditions.
		// but we can use reflection to call the createReportEngine method
		// and get the commercial report engine running as opposed to the
		// open source one, which
		// will give access to all the commercial emitters
		final Class<?> factoryClass = factoryObjectForReflection.getClass();
		final Method[] methods = factoryClass.getDeclaredMethods();
		IReportEngine reportEngine = null;
		for (final Method m : methods) {
			final String name = m.getName();
			m.setAccessible(true);
			if (name.equals("createReportEngine")) {
				reportEngine = (IReportEngine) m.invoke(factoryObjectForReflection, config);
			}
		}
		return reportEngine;
	}

	public IDesignEngine getDesignEngine() throws BirtException {
		final DesignConfig designConfig = new DesignConfig();
		designConfig.setBIRTHome(birtRuntimeHome.getAbsolutePath());
		Platform.startup(designConfig);
		final IDesignEngineFactory dfactory = (IDesignEngineFactory) Platform.createFactoryObject(
			IDesignEngineFactory.EXTENSION_DESIGN_ENGINE_FACTORY);
		final IDesignEngine designEngine = dfactory.createDesignEngine(designConfig);
		return designEngine;
	}

	/*
	 * The engine needs to see a list of each jar file concatenated as a string
	 * using the standard file system separator to divide the files
	 */
	private String getScriptLibFileNames() {
		if (scriptLib == null) {
			return null;
		}
		if (!scriptLib.exists()) {
			scriptLib.mkdirs();
		}
		final File[] files = scriptLib.listFiles(new JarFilter());
		final StringBuilder sb = new StringBuilder();
		String sep = "";
		final String fileSeparatorString = new String(new char[] { File.pathSeparatorChar });
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				sb.append(sep);
				sep = fileSeparatorString;
				sb.append(files[i].getAbsolutePath());
			}
		}
		return sb.toString();
	}

	private static class JarFilter implements FilenameFilter {
		private final String extension = ".jar";

		@Override
		public boolean accept(final File dir, final String name) {
			return name.toLowerCase().endsWith(extension);
		}
	}
}
