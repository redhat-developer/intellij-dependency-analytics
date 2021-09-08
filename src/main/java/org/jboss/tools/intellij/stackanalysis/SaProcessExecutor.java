/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.stackanalysis;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.SystemInfo;
import org.jboss.tools.intellij.analytics.Platform;
import org.jboss.tools.intellij.analytics.PlatformDetectionException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;


public class SaProcessExecutor {
	private static final Logger logger = Logger.getInstance(SaProcessExecutor.class);
	private static final String CLI_CONFIG_FILE_PATH = Paths.get(System.getProperty("user.home"),
			".crda", "config.yaml").toString();
	private static final String CLI_COMMAND =  SystemInfo.isLinux ? "./crda" : "crda";


	/**
	 * <p>Authenticate a CRDA CLI user.</p>
	 *
	 * If CLI config file is not present in host machine then user is considered to be new
	 * and needs to be authenticated in CRDA Platform.
	 *
	 * @throws IOException In case of process failure
	 * @throws InterruptedException In case of process failure
	 */
	public void authenticateUser() throws IOException, InterruptedException {
		// Check if crda config file is already present in system. if not create CLI config file.
		if(!Files.exists(Paths.get(CLI_CONFIG_FILE_PATH))) {
			logger.info("Authenticating user.");

			// Run CLI command to set user consent to False for CLI telemetry data collection.
			execute(new String[]{CLI_COMMAND, "config", "set", "consent_telemetry", "false"});

			// Run command to authenticate user in CRDA Platform.
			execute(new String[]{CLI_COMMAND, "config", "set", "crda_key", UUID.randomUUID().toString()});
		}
	}


	/**
	 * <p>Perform Stack Analysis on given file.</p>
	 *
	 * @param filePath Path to target manifest file.
	 *
	 * @return String object having analysis report.
	 *
	 * @throws IOException In case of process failure
	 * @throws InterruptedException In case of process failure
	 */
	public String performStackAnalysis(String filePath) throws IOException, InterruptedException {
		logger.info("Starting Stack Analysis.");
		// Authenticate user, in case file has been deleted after loading the plugin
		authenticateUser();

		// Execute CLI command for analysis
		return execute(new String[]{CLI_COMMAND, "analyse", filePath, "-j", "-m", "intellij"});
	}


	/**
	 * <p>Execute CLI commands.</p>
	 *
	 * @param arguments Arguments for command to be executed.
	 *
	 * @return String object having result of command.
	 *
	 * @throws IOException In case of process failure
	 * @throws InterruptedException In case of process failure
	 */
	public String execute(String[] arguments) throws IOException, InterruptedException {
		// Logic to execute given CLI command and get the result.
		ProcessBuilder processBuilder = new ProcessBuilder(arguments);

		// Set CLI binary location as working directory for process.
		processBuilder.directory(new File(Platform.pluginDirectory));
		processBuilder.redirectErrorStream(true);

		Process process = processBuilder.start();
		StringBuilder output = new StringBuilder();

		try (InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8);
			 BufferedReader bufferedReader = new BufferedReader(inputStreamReader)){

			String line;
			while ((line = bufferedReader.readLine()) != null) {
				output.append(line).append("\n");
			}

			int exitVal = process.waitFor();

			// Return data according to exit code of command.
			if (exitVal == 0 || exitVal == 2) {
				return output.toString();
			} else {
				logger.info("Process execution failed for = "+processBuilder.command());
				throw new PlatformDetectionException(output.toString());
			}
		}
	}
}
