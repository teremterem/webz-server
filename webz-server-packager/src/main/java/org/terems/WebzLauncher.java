/*
 * WebZ Server can serve web pages from various local and remote file sources.
 * Copyright (C) 2014-2015  Oleksandr Tereschenko <http://www.terems.org/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.terems;

import java.awt.Desktop;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import javax.servlet.ServletException;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.http.fileupload.IOUtils;

public class WebzLauncher {

	private static final String HELP_ARG = "help";
	private static final String NO_GUI_ARG = "no-gui";

	private static final String HTTP_PORT_PROPERTY = "webz.http.port";
	private static final String DEFAULT_HTTP_PORT = "8887";

	private static final Pattern WEBZ_WAR_PATTERN = Pattern.compile("webz-[^/\\\\]*.war");

	private static final int GENERIC_FATAL_EXIT_CODE = 1;
	private static final int PORT_BUSY_FATAL_EXIT_CODE = 2;

	private static boolean help = false;
	private static boolean gui = true;

	public static void main(String[] args) {

		processArgs(args);
		gui = gui && !(help || isHeadlessSafe());

		if (gui) {
			WebzLauncherGUI.initGuiSafe(WebzLaunchHelper.SERVER_TITLE + " (" + WebzLaunchHelper.VERSION_TITLE + ")");
		}
		File thisJarFile = null;
		try {
			thisJarFile = getThisJarFile();

			if (help) {
				printHelp(thisJarFile.getName());
			} else {
				prepareAndRun(thisJarFile);
			}

		} catch (Throwable th) {

			th.printStackTrace();
			if (gui) {
				WebzLauncherGUI.showFatalAndExit(GENERIC_FATAL_EXIT_CODE,
						formatFatalMessage(th, thisJarFile == null ? null : thisJarFile.getName()));
			} else {
				System.exit(GENERIC_FATAL_EXIT_CODE);
			}
		}
	}

	private static void processArgs(String[] args) {

		for (String arg : args) {

			if (HELP_ARG.equals(arg)) {
				help = true;

			} else if (NO_GUI_ARG.equals(arg)) {
				gui = false;
			}
		}
	}

	private static FileOutputStream logFile = null;

	private static void prepareAndRun(File thisJarFile) throws URISyntaxException, IOException, ServletException, LifecycleException {

		Properties webzProperties = WebzLaunchHelper.initWebzPropertiesInLauncher(thisJarFile);
		int configuredPortNumber = getConfiguredPortNumber(webzProperties);

		File serverFolder = initServerFolder(thisJarFile, configuredPortNumber);
		logFile = StdErrOutLogger.install(new File(serverFolder, "webz-server.log"));

		Tomcat tomcat = prepareTomcat(thisJarFile, serverFolder);
		int actualPortNumber = runTomcat(tomcat, configuredPortNumber);
		if (actualPortNumber < 0) {

			if (gui) {
				String thisJarName = thisJarFile.getName();

				WebzLauncherGUI.showFatalAndExit(PORT_BUSY_FATAL_EXIT_CODE, "Port " + configuredPortNumber
						+ " is already in use.\n\nTry setting a different port:\n" + getStartAtSpecificPortCommandHint(thisJarName)
						+ "\n\n" + getUsageOptionsAdditionalHint(thisJarName));
			} else {
				System.exit(PORT_BUSY_FATAL_EXIT_CODE);
			}

		} else {

			if (gui) {
				WebzLauncherGUI.showServerStartedSafe(actualPortNumber);
				openBrowserSafe(actualPortNumber);
			}
			// // ~~~ \\ // ~~~ \\ //
			tomcat.getServer().await();
			// \\ ~~~ // \\ ~~~ // \\
		}
	}

	private static int getConfiguredPortNumber(Properties webzProperties) {

		String port = System.getProperty(HTTP_PORT_PROPERTY, webzProperties.getProperty(HTTP_PORT_PROPERTY, DEFAULT_HTTP_PORT));
		int portNumber = Integer.valueOf(port);

		if (portNumber > 0) {
			return portNumber;
		} else {
			throw new RuntimeException(portNumber + " is not a valid port number (should be greater than zero)");
		}
	}

	private static Tomcat prepareTomcat(File thisJarFile, File serverFolder) throws ServletException, IOException {

		createFolder(serverFolder, "webapps");
		// Tomcat wants this folder to be there before it starts

		Tomcat tomcat = new Tomcat();
		tomcat.setBaseDir(serverFolder.getAbsolutePath());

		tomcat.addWebapp("", fetchWebzWar(thisJarFile, serverFolder).getAbsolutePath());

		return tomcat;
	}

	private static int runTomcat(final Tomcat tomcat, int portNumber) throws LifecycleException {

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {

				try {
					tomcat.stop();
					tomcat.destroy();

				} catch (LifecycleException e) {
					throw new RuntimeException(e);

				} finally {
					if (logFile != null) {
						try {
							logFile.close();
						} catch (IOException e) {
							// ignore
						}
					}
				}
			}
		}));

		tomcat.setPort(portNumber);
		tomcat.start();

		return tomcat.getConnector().getLocalPort();
	}

	private static File fetchWebzWar(File thisJarFile, File serverFolder) throws IOException {

		File webzWarFile = null;
		JarFile thisJar = null;
		try {
			thisJar = new JarFile(thisJarFile);

			Enumeration<JarEntry> jarEntries = thisJar.entries();
			while (jarEntries.hasMoreElements()) {

				JarEntry jarEntry = jarEntries.nextElement();
				if (!jarEntry.isDirectory()) {
					String jarEntryName = jarEntry.getName();

					if (WEBZ_WAR_PATTERN.matcher(jarEntryName).matches()) {

						webzWarFile = extractResourceIntoFolder(jarEntryName, serverFolder);
						break;
					}
				}
			}
		} finally {
			if (thisJar != null) {
				try {
					thisJar.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}

		if (webzWarFile == null) {
			throw new RuntimeException("webz-{version}.war was not found in the jar");
		}
		return webzWarFile;
	}

	private static File getThisJarFile() throws URISyntaxException {

		CodeSource codeSource = WebzLauncher.class.getProtectionDomain().getCodeSource();
		if (codeSource == null) {
			throw new RuntimeException("CodeSource is null");
		}
		File thisJarFile = new File(codeSource.getLocation().toURI());
		return thisJarFile;
	}

	private static File initServerFolder(File thisJarFile, int portNumber) throws URISyntaxException {

		File parentFolder = thisJarFile.getParentFile();
		String thisJarName = thisJarFile.getName();

		if (thisJarName == null || thisJarName.length() <= 0) {
			throw new RuntimeException("empty or null launching jar name");
		}
		if (thisJarName.toLowerCase().endsWith(".jar")) {
			thisJarName = thisJarName.substring(0, thisJarName.length() - 4);
		}
		return createFolder(parentFolder, thisJarName + ".port-" + portNumber);
	}

	private static File createFolder(File parentFolder, String name) {
		return createFolder(new File(parentFolder, name));
	}

	private static File createFolder(File folder) {

		if (folder.exists()) {

			if (!folder.isDirectory()) {
				throw new RuntimeException(folder.getAbsolutePath() + " already exists and is NOT a folder");
			}

		} else if (!folder.mkdir()) {
			throw new RuntimeException("failed to create folder: " + folder.getAbsolutePath());
		}

		return folder;
	}

	private static File extractResourceIntoFolder(String resourceName, File folder) throws IOException {

		if (!resourceName.startsWith("/")) {
			resourceName = "/" + resourceName;
		}

		InputStream in = WebzLauncher.class.getResourceAsStream(resourceName);
		if (in == null) {
			throw new RuntimeException(resourceName + " was not found in the jar");
		}

		File file = new File(folder, resourceName);

		OutputStream out = null;
		try {
			out = new FileOutputStream(file);
			IOUtils.copy(in, out);

		} finally {

			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// ignore
				}
			}
			try {
				in.close();
			} catch (IOException e) {
				// ignore
			}
		}

		return file;
	}

	private static boolean isHeadlessSafe() {

		try {
			if (GraphicsEnvironment.isHeadless()) {
				return true;
			}
			GraphicsDevice[] screenDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
			return screenDevices == null || screenDevices.length == 0;

		} catch (Throwable th) {
			th.printStackTrace();
			return true;
		}
	}

	private static void openBrowserSafe(int httpPortNumber) {

		try {
			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().browse(new URI("http://localhost" + (httpPortNumber == 80 ? "" : ":" + httpPortNumber) + "/"));
			}
		} catch (Throwable th) {
			// ignore
		}
	}

	private static String formatFatalMessage(Throwable th, String thisJarName) {

		String message = th.toString() + "\n\nStart in a console to see the stack trace";
		if (thisJarName == null) {
			return message + ".";
		} else {
			return message + ":\n" + getSimpleStartCommandHint(thisJarName) + "\n\n" + getUsageOptionsAdditionalHint(thisJarName);
		}
	}

	private static String getUsageOptionsAdditionalHint(String thisJarName) {
		return "Also, if you want to see different usage options, try the following:\n" + getHelpCommandHint(thisJarName);
	}

	private static String getSimpleStartCommandHint(String thisJarName) {
		return "> java -jar " + thisJarName;
	}

	private static String getStartAtSpecificPortCommandHint(String thisJarName) {
		return "> java -D" + HTTP_PORT_PROPERTY + "={port} -jar " + thisJarName;
	}

	private static String getHelpCommandHint(String thisJarName) {
		return "> java -jar " + thisJarName + " " + HELP_ARG;
	}

	private static void printHelp(String thisJarName) {

		System.out.println();
		System.out.println();
		System.out.println("    Usage examples:");
		System.out.println();
		System.out.println();

		System.out.println(getHelpCommandHint(thisJarName));
		System.out.println();
		System.out.println("Show this help.");
		System.out.println();
		System.out.println();

		System.out.println(getSimpleStartCommandHint(thisJarName));
		System.out.println();
		System.out.println("Start with GUI.");
		System.out.println();
		System.out.println();

		System.out.println("> java -jar " + thisJarName + " " + NO_GUI_ARG);
		System.out.println();
		System.out.println("Start without GUI.");
		System.out.println();
		System.out.println();

		System.out.println(getStartAtSpecificPortCommandHint(thisJarName));
		System.out.println();
		System.out.println("Listen to a specific {port} number (by default the port number is " + DEFAULT_HTTP_PORT + ").");
		System.out.println();
		System.out.println();

		System.out.println("> java -D" + WebzLaunchHelper.WEBZ_PROPERTIES_PROPERTY + "={pathname} -jar " + thisJarName);
		System.out.println();
		System.out.println("Read WebZ properties from {pathname} (by default it attempts to find a file with the name '"
				+ WebzLaunchHelper.WEBZ_PROPERTIES_DEFAULT_FILENAME + "' in the folder where this jar is located).");
		System.out.println("Another way to supply WebZ properties pathname is to set " + WebzLaunchHelper.WEBZ_PROPERTIES_ENV_VAR
				+ " environment variable before running the jar.");
		System.out.println();
		System.out.println();

		System.out.println();
		System.out.println("    WebZ properties:");
		System.out.println();
		System.out.println();
		System.out.println(WebzLaunchHelper.WEBZ_BOILERPLATE_PATH_PROPERTY + "={path-to-webz-boilerplate-root-folder}");
		System.out.println(WebzLaunchHelper.RENDERING_SPA_PATH_PROPERTY + "={path-to-SPA-root-folder}");
		System.out.println(WebzLaunchHelper.SITE_CONTENT_PATH_PROPERTY + "={path-to-site-content-root-folder}");
		System.out.println();
		System.out.println("# Optional:");
		System.out.println("#" + HTTP_PORT_PROPERTY + "=" + DEFAULT_HTTP_PORT);
		System.out.println();
		System.out.println();
	}
}