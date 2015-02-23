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

	private static final String DEFAULT_HTTP_PORT = "8887";

	private static final Pattern WEBZ_WAR_PATTERN = Pattern.compile("webz-[^/\\\\]*.war");

	private static final String LAUNCH_FAILURE_MSG_PREFIX = "Failed to launch WebZ Server: ";

	private static File tempFolder;

	public static void main(String[] args) throws URISyntaxException, IOException, ServletException, LifecycleException {

		CodeSource codeSource = WebzLauncher.class.getProtectionDomain().getCodeSource();
		if (codeSource == null) {
			throw new RuntimeException(LAUNCH_FAILURE_MSG_PREFIX + "CodeSource is null");
		}
		File thisJarFile = new File(codeSource.getLocation().toURI());

		initTempFolder(thisJarFile);

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

						webzWarFile = putWebzWarIntoTemp(jarEntryName);
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
			throw new RuntimeException(LAUNCH_FAILURE_MSG_PREFIX + "webz.war was not found in the jar");
		}

		createFolder(tempFolder, "webapps");
		// Tomcat wants this folder to be there

		final Tomcat tomcat = new Tomcat();

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

		tomcat.setBaseDir(tempFolder.getAbsolutePath());
		tomcat.setSilent(true);

		String port = System.getenv("PORT");
		if (port == null || port.isEmpty()) {
			port = DEFAULT_HTTP_PORT;
		}
		int httpPortNumber = Integer.valueOf(port);
		// TODO make WebZ Server log port number instead of Tomcat

		tomcat.setPort(httpPortNumber);

		Context webzContext = tomcat.addWebapp("", webzWarFile.getAbsolutePath());
		webzContext.setJarScanner(new JarScanner() {
			@Override
			public void scan(ServletContext context, ClassLoader classloader, JarScannerCallback callback, Set<String> jarsToSkip) {
				// no need to scan jars - saving CPU time...
			}
		});

		tomcat.start();

		try {
			FileUtils.forceDeleteOnExit(tempFolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().browse(new URI("http://localhost" + (httpPortNumber == 80 ? "" : ":" + httpPortNumber) + "/"));
			}
		} catch (Throwable th) {
			// ignore
		}

		tomcat.getServer().await();
	}

	private static void initTempFolder(File thisJarFile) throws URISyntaxException {

		File parentFolder = thisJarFile.getParentFile();
		String thisJarName = thisJarFile.getName();

		if (thisJarName == null || thisJarName.length() <= 0) {
			throw new RuntimeException(LAUNCH_FAILURE_MSG_PREFIX + "empty or null launching jar name");
		}
		if (thisJarName.toLowerCase().endsWith(".jar")) {
			thisJarName = thisJarName.substring(0, thisJarName.length() - 4);
		}
		tempFolder = createFolder(parentFolder, "." + thisJarName + ".temp");
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

	private static File putWebzWarIntoTemp(String webzWarName) {

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
			throw new RuntimeException(LAUNCH_FAILURE_MSG_PREFIX + e.getMessage(), e);
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

}