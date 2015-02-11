/*
 * WebZ Server is a server that can serve web pages from various sources.
 * Copyright (C) 2013-2015  Oleksandr Tereschenko <http://www.terems.org/>
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

package org.terems.webz.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzMetadata;
import org.terems.webz.WebzReadException;
import org.terems.webz.WebzWriteException;
import org.terems.webz.base.BaseWebzFilter;
import org.terems.webz.internals.WebzIdentifiable;

/** TODO !!! describe !!! **/
public class WebzUtils {

	public static final char FILE_EXT_SEPARATOR = '.';

	private static final int DEFAULT_BUFFER_SIZE = 8192;

	/** TODO !!! describe !!! **/
	public static String getFullUrl(HttpServletRequest req) {

		StringBuffer requestUrl = req.getRequestURL();
		String queryString = req.getQueryString();

		if (queryString == null) {
			return requestUrl.toString();
		} else {
			return requestUrl.append('?').append(queryString).toString();
		}
	}

	/** TODO !!! describe !!! **/
	public static String formatRequestMethodAndUrl(HttpServletRequest req) {
		return req.getMethod() + " " + getFullUrl(req);
	}

	/** TODO !!! describe !!! **/
	public static long copyInToOut(InputStream in, OutputStream out) throws WebzReadException, WebzWriteException {

		long bytesTotal = 0;

		byte[] buff = new byte[DEFAULT_BUFFER_SIZE];

		while (true) {
			int bytes;

			try {
				bytes = in.read(buff);
			} catch (IOException e) {
				throw new WebzReadException(e);
			}

			if (bytes < 1) {
				break;
			}

			try {
				out.write(buff, 0, bytes);
			} catch (IOException e) {
				throw new WebzWriteException(e);
			}

			bytesTotal += bytes;
		}

		return bytesTotal;
	}

	/** TODO !!! describe !!! **/
	public static void closeSafely(Closeable resource) {

		if (resource != null) {
			try {
				resource.close();
			} catch (IOException e) {
				// ignoring...
			}
		}
	}

	/** TODO !!! describe !!! **/
	public static boolean containsUpperCaseLetters(String value) {

		int len = value.length();
		for (int i = 0; i < len;) {

			int codePoint = value.codePointAt(i);
			if (Character.isUpperCase(codePoint)) {
				return true;
			}

			i += Character.charCount(codePoint);
		}

		return false;
	}

	private static final ClassLoader DEFAULT_CLASS_LOADER = WebzUtils.class.getClassLoader();

	/** TODO !!! describe !!! **/
	public static Properties loadPropertiesFromClasspath(String resourceName, boolean failIfNotFound) throws WebzException {
		return loadPropertiesFromClasspath(resourceName, DEFAULT_CLASS_LOADER, failIfNotFound);
	}

	/** TODO !!! describe !!! **/
	public static void loadPropertiesFromClasspath(Properties properties, String resourceName, boolean failIfNotFound) throws WebzException {
		loadPropertiesFromClasspath(properties, resourceName, DEFAULT_CLASS_LOADER, failIfNotFound);
	}

	/** TODO !!! describe !!! **/
	public static Properties loadPropertiesFromClasspath(String resourceName, ClassLoader classLoader, boolean failIfNotFound)
			throws WebzException {

		Properties properties = new Properties();
		loadPropertiesFromClasspath(properties, resourceName, classLoader, failIfNotFound);

		return properties;
	}

	/** TODO !!! describe !!! **/
	public static void loadPropertiesFromClasspath(Properties properties, String resourceName, ClassLoader classLoader,
			boolean failIfNotFound) throws WebzException {

		if (resourceName == null) {
			throw new NullPointerException("null resource name was supplied - properties cannot be read");
		}

		InputStream in = classLoader.getResourceAsStream(resourceName);
		try {
			if (in == null) {
				if (failIfNotFound) {
					throw new WebzException("'" + resourceName + "' was not found on classpath");
				}
				return;
			}
			properties.load(in);

		} catch (IOException e) {
			throw new WebzException("failed to read '" + resourceName + "' from classpath: " + e.getMessage(), e);

		} finally {
			closeSafely(in);
		}
	}

	/** TODO !!! describe !!! **/
	public static Properties loadProperties(WebzFile file, boolean failIfNotFound) throws WebzException {

		Properties properties = new Properties();
		loadProperties(properties, file, failIfNotFound);

		return properties;
	}

	/** TODO !!! describe !!! **/
	public static void loadProperties(Properties properties, WebzFile file, boolean failIfNotFound) throws WebzException {

		if (file == null) {
			throw new NullPointerException("null WebzFile was supplied - properties cannot be read");
		}

		WebzFileDownloader fileDownloader = null;
		try {
			fileDownloader = file.getFileDownloader();

			if (fileDownloader == null) {
				if (failIfNotFound) {
					throw new WebzException("'" + file.getPathname() + "' was not found");
				}
				return;
			}
			properties.load(fileDownloader.content);

		} catch (IOException e) {
			throw new WebzException("failed to read '" + file.getPathname() + "': " + e.getMessage(), e);

		} finally {
			if (fileDownloader != null) {
				fileDownloader.close();
			}
		}
	}

	/** TODO !!! describe !!! **/
	public static String getFileExtension(WebzMetadata metadata) throws WebzException {

		if (metadata != null) {
			try {
				String fileName = metadata.getName();
				int i = fileName.lastIndexOf(FILE_EXT_SEPARATOR);

				if (i > -1) {
					return fileName.substring(i + 1);
				}
			} catch (IOException e) {
				throw new WebzException(e);
			}
		}
		return null;
	}

	/** TODO !!! describe !!! **/
	public static String assertString(Object value) {

		if (!(value instanceof String)) {
			throw new ClassCastException(value + " is not of type String");
		}
		return (String) value;
	}

	private static final String WEBZ_FILE_SYSTEM_ID_MSG = "WebzFileSystem ID";

	/** TODO !!! describe !!! **/
	public static String formatFileSystemMessage(String message, WebzIdentifiable identifiable) {
		return message + " (" + WEBZ_FILE_SYSTEM_ID_MSG + ": '" + identifiable.getUniqueId() + "')";
	}

	/** TODO !!! describe !!! **/
	public static String formatFileSystemMessageNoBrackets(String message, WebzIdentifiable identifiable) {
		return message + " - " + WEBZ_FILE_SYSTEM_ID_MSG + ": '" + identifiable.getUniqueId() + "'";
	}

	/** TODO !!! describe !!! **/
	public static String replaceWhitespacesWithDashesSafely(String value) {

		if (value == null) {
			return null;
		}
		return value.trim().replaceAll("\\s+", "-");
	}

	private static final ThreadLocal<DateFormat> HTTP_DATE_FORMAT = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {

			DateFormat value = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss.SSS z", Locale.US);
			value.setTimeZone(TimeZone.getTimeZone("GMT"));
			return value;
		}
	};

	/** TODO !!! describe !!! **/
	public static String formatHttpDate(long date) {
		return formatHttpDate(new Date(date));
	}

	/** TODO !!! describe !!! **/
	public static String formatHttpDate(Date date) {
		return HTTP_DATE_FORMAT.get().format(date);
	}

	/** TODO !!! describe !!! **/
	public static String[] parseCsv(String csv) {

		if (csv == null) {
			return new String[] {};
		}
		// TODO support proper csv unescaping
		return csv.split("\\s*,\\s*");
	}

	/** TODO !!! describe !!! **/
	public static boolean isHttpMethodHead(HttpServletRequest req) {
		return BaseWebzFilter.HTTP_HEAD.equals(req.getMethod());
	}

	/** TODO !!! describe !!! **/
	public static boolean isHttpMethodGet(HttpServletRequest req) {
		return BaseWebzFilter.HTTP_GET.equals(req.getMethod());
	}

	/** TODO !!! describe !!! **/
	public static String toLowerCaseEng(String value) {

		if (value == null) {
			return null;
		}
		return value.toLowerCase(Locale.ENGLISH);
	}

}
