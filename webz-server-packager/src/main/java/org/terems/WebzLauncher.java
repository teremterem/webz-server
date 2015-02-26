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
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.JarScannerCallback;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;

public class WebzLauncher {

	private static final String HELP_ARG = "help";
	private static final String NO_GUI_ARG = "no-gui";

	private static final String HTTP_PORT_PROPERTY = "webz.http.port";

	private static final String DEFAULT_HTTP_PORT = "8887";

	private static final Pattern WEBZ_WAR_PATTERN = Pattern.compile("webz-[^/\\\\]*.war");

	private static final String LAUNCH_FAILURE_MSG_PREFIX = "Failed to launch WebZ Server: ";

	private static boolean help = false;
	private static boolean gui = true;

	public static void main(String[] args) {

		processArgs(args);

		gui = gui && !(help || isHeadlessSafe());
		if (gui) {
			WebzLauncherGUI.initGuiSafe("WebZ Server v0.9 beta (Pedesis)");
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
				WebzLauncherGUI.showFatalAndShutdownSafe(formatFatalMessage(th, thisJarFile == null ? null : thisJarFile.getName()));
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

	private static void printHelp(String thisJarName) {

		System.out.println("\nUsage examples:\n");

		System.out.println(getSimpleStartCommandHint(thisJarName));
		System.out.println("\tstart with GUI\n");

		System.out.println("> java -jar " + thisJarName + " " + NO_GUI_ARG);
		System.out.println("\tstart without GUI\n");

		System.out.println(getStartAtSpecificPortCommandHint(thisJarName));
		System.out.println("\tlisten to a specific port number (by default the port number is " + DEFAULT_HTTP_PORT + ")\n");

		System.out.println(getHelpCommandHint(thisJarName));
		System.out.println("\tshow this help\n");
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

	private static void prepareAndRun(File thisJarFile) throws URISyntaxException, IOException, ServletException, LifecycleException {

		int configuredPortNumber = getConfiguredPortNumber();

		File tempFolder = initTempFolder(thisJarFile, configuredPortNumber);
		createFolder(tempFolder, "webapps");
		// Tomcat wants this folder to be there before it starts

		Tomcat tomcat = prepareTomcat(thisJarFile, tempFolder);
		int actualPortNumber = runTomcat(tomcat, configuredPortNumber);
		// TODO make WebZ Server write it's log into a file

		if (actualPortNumber < 0) {

			if (gui) {
				String thisJarName = thisJarFile.getName();

				WebzLauncherGUI.showFatalAndShutdownSafe("Port " + configuredPortNumber
						+ " is already in use.\n\nTry setting a different port:\n" + getStartAtSpecificPortCommandHint(thisJarName)
						+ "\n\n" + getUsageOptionsAdditionalHint(thisJarName));
			}

		} else {
			try {
				FileUtils.forceDeleteOnExit(tempFolder);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (gui) {
				WebzLauncherGUI.showServerStartedSafe(actualPortNumber);
				openBrowserSafe(actualPortNumber);
			}
			tomcat.getServer().await();
		}
	}

	private static int getConfiguredPortNumber() {

		// TODO make port number configurable through webz.properties as well
		String port = System.getProperty(HTTP_PORT_PROPERTY);
		if (port == null || port.isEmpty()) {
			port = DEFAULT_HTTP_PORT;
		}
		return Integer.valueOf(port);
	}

	private static Tomcat prepareTomcat(File thisJarFile, File tempFolder) throws ServletException, IOException {

		Tomcat tomcat = new Tomcat();
		tomcat.setBaseDir(tempFolder.getAbsolutePath());

		Context webzContext = tomcat.addWebapp("", fetchWebzWar(thisJarFile, tempFolder).getAbsolutePath());
		webzContext.setJarScanner(new JarScanner() {
			@Override
			public void scan(ServletContext context, ClassLoader classloader, JarScannerCallback callback, Set<String> jarsToSkip) {
				// no need to scan jars - saving CPU time...
			}
		});
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
				}
			}
		}));

		tomcat.setPort(portNumber);
		tomcat.start();

		return tomcat.getConnector().getLocalPort();
	}

	private static File fetchWebzWar(File thisJarFile, File tempFolder) throws IOException {

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

						webzWarFile = putWebzWarIntoTemp(jarEntryName, tempFolder);
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
			throw new RuntimeException(LAUNCH_FAILURE_MSG_PREFIX + "webz-{version}.war was not found in the jar");
		}
		return webzWarFile;
	}

	private static File getThisJarFile() throws URISyntaxException {

		CodeSource codeSource = WebzLauncher.class.getProtectionDomain().getCodeSource();
		if (codeSource == null) {
			throw new RuntimeException(LAUNCH_FAILURE_MSG_PREFIX + "CodeSource is null");
		}
		File thisJarFile = new File(codeSource.getLocation().toURI());
		return thisJarFile;
	}

	private static File initTempFolder(File thisJarFile, int portNumber) throws URISyntaxException {

		File parentFolder = thisJarFile.getParentFile();
		String thisJarName = thisJarFile.getName();

		if (thisJarName == null || thisJarName.length() <= 0) {
			throw new RuntimeException(LAUNCH_FAILURE_MSG_PREFIX + "empty or null launching jar name");
		}
		if (thisJarName.toLowerCase().endsWith(".jar")) {
			thisJarName = thisJarName.substring(0, thisJarName.length() - 4);
		}
		return createFolder(parentFolder, "." + thisJarName + ".port-" + portNumber + ".temp");
	}

	private static File createFolder(File parentFolder, String name) {

		File folder = new File(parentFolder, name);

		if (folder.exists()) {

			if (!folder.isDirectory()) {
				throw new RuntimeException(LAUNCH_FAILURE_MSG_PREFIX + folder.getAbsolutePath() + " already exists and is NOT a folder");
			}

		} else if (!folder.mkdir()) {
			throw new RuntimeException(LAUNCH_FAILURE_MSG_PREFIX + "failed to create folder: " + folder.getAbsolutePath());
		}

		return folder;
	}

	private static File putWebzWarIntoTemp(String webzWarName, File tempFolder) {

		String resourceName = "/" + webzWarName;
		InputStream in = WebzLauncher.class.getResourceAsStream(resourceName);
		if (in == null) {
			throw new RuntimeException(LAUNCH_FAILURE_MSG_PREFIX + resourceName + " was not found in the jar");
		}

		File file = new File(tempFolder, webzWarName);

		OutputStream out = null;
		try {
			out = new FileOutputStream(file);
			IOUtils.copy(in, out);

		} catch (IOException e) {
			throw new RuntimeException(LAUNCH_FAILURE_MSG_PREFIX + e.toString(), e);
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
		String message = th.toString() + "\n\nTry starting in a console to see the full stack trace";

		if (thisJarName == null) {
			return message + ".";
		} else {
			return message + ":\n" + getSimpleStartCommandHint(thisJarName) + "\n\n" + getUsageOptionsAdditionalHint(thisJarName);
		}
	}

	private static String getUsageOptionsAdditionalHint(String thisJarName) {
		return "Also, if you want to see different usage options, try the following:\n" + getHelpCommandHint(thisJarName);
	}

}