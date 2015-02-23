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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.apache.catalina.startup.Tomcat;

public class WebzLauncher {

	private static final String DEFAULT_HTTP_PORT = "8887";

	private static final String TOMCAT_JARS_FOLDER = "tomcat/";
	private static final Pattern WEBZ_WAR_PATTERN = Pattern.compile("webz-[^/\\\\]*.war");

	private static final String LAUNCH_FAILURE_MSG_PREFIX = "Failed to launch WebZ Server: ";

	private static final int DEFAULT_BUFFER_SIZE = 8192;

	private static File tempFolder;

	public static void main(String[] args) throws Exception {

		CodeSource codeSource = WebzLauncher.class.getProtectionDomain().getCodeSource();
		if (codeSource == null) {
			throw new RuntimeException(LAUNCH_FAILURE_MSG_PREFIX + "CodeSource is null");
		}
		File thisJarFile = new File(codeSource.getLocation().toURI());

		initTempFolder(thisJarFile);

		// ~

		Collection<File> tomcatJarFiles = new ArrayList<File>();
		File webzWarFile = null;

		JarFile thisJar = null;
		try {
			thisJar = new JarFile(thisJarFile);

			Enumeration<JarEntry> jarEntries = thisJar.entries();
			while (jarEntries.hasMoreElements()) {

				JarEntry jarEntry = jarEntries.nextElement();
				if (!jarEntry.isDirectory()) {
					String jarEntryName = jarEntry.getName();

					if (jarEntryName.startsWith(TOMCAT_JARS_FOLDER)) {

						tomcatJarFiles.add(putResourceIntoTemp(jarEntryName, jarEntryName.substring(TOMCAT_JARS_FOLDER.length())));

					} else if (webzWarFile == null && WEBZ_WAR_PATTERN.matcher(jarEntryName).matches()) {

						webzWarFile = putResourceIntoTemp(jarEntryName, jarEntryName);

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

		Tomcat tomcat = putJarsToClasspathAndCreateTomcat(tomcatJarFiles);

		tomcat.setBaseDir(tempFolder.getAbsolutePath());
		tomcat.setSilent(true);

		String port = System.getenv("PORT");
		if (port == null || port.isEmpty()) {
			port = DEFAULT_HTTP_PORT;
		}
		// TODO make WebZ Server log port number instead of Tomcat

		tomcat.setPort(Integer.valueOf(port));

		tomcat.addWebapp("/", webzWarFile.getAbsolutePath());

		tomcat.start();
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
		tempFolder = new File(parentFolder, thisJarName + "-temp");
		tempFolder.deleteOnExit();

		if (tempFolder.exists()) {

			if (!tempFolder.isDirectory()) {
				throw new RuntimeException(LAUNCH_FAILURE_MSG_PREFIX + "temp folder path already exists and is NOT a folder: "
						+ tempFolder.getAbsolutePath());
			}

		} else if (!tempFolder.mkdir()) {
			throw new RuntimeException(LAUNCH_FAILURE_MSG_PREFIX + "failed to create temp folder: " + tempFolder.getAbsolutePath());
		}
	}

	private static Tomcat putJarsToClasspathAndCreateTomcat(Collection<File> jarFiles) throws MalformedURLException,
			ClassNotFoundException, InstantiationException, IllegalAccessException {

		URL[] jarFileUrls = new URL[jarFiles.size()];

		Iterator<File> it = jarFiles.iterator();
		for (int i = 0; it.hasNext(); i++, it.next()) {
			jarFileUrls[i] = it.next().toURI().toURL();
		}
		URLClassLoader childLoader = URLClassLoader.newInstance(jarFileUrls, WebzLauncher.class.getClassLoader());

		Class<?> tomcatClass = Class.forName("org.apache.catalina.startup.Tomcat", true, childLoader);

		return (Tomcat) tomcatClass.newInstance();
	}

	private static File putResourceIntoTemp(String resourceName, String asFileName) {

		InputStream in = WebzLauncher.class.getResourceAsStream("/" + resourceName);
		if (in == null) {
			throw new RuntimeException(LAUNCH_FAILURE_MSG_PREFIX + resourceName + " was not found in the jar");
		}

		File file = new File(tempFolder, asFileName);
		file.deleteOnExit();

		OutputStream out = null;
		try {
			out = new FileOutputStream(file);

			byte[] buff = new byte[DEFAULT_BUFFER_SIZE];
			while (true) {
				int bytes = in.read(buff);
				if (bytes < 1) {
					break;
				}
				out.write(buff, 0, bytes);
			}

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