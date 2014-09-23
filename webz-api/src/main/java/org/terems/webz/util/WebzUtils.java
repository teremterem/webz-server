package org.terems.webz.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzFileSystem;
import org.terems.webz.WebzMetadata;
import org.terems.webz.WebzReadException;
import org.terems.webz.WebzWriteException;

/** TODO !!! describe !!! **/
public class WebzUtils {

	public static final char FILE_EXT_SEPARATOR = '.';

	private static final int DEFAULT_BUFFER_SIZE = 8192;

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
	public static Properties loadPropertiesFromClasspath(String resourceName) throws WebzException {
		return loadPropertiesFromClasspath(resourceName, DEFAULT_CLASS_LOADER);
	}

	/** TODO !!! describe !!! **/
	public static void loadPropertiesFromClasspath(Properties properties, String resourceName) throws WebzException {
		loadPropertiesFromClasspath(properties, resourceName, DEFAULT_CLASS_LOADER);
	}

	/** TODO !!! describe !!! **/
	public static Properties loadPropertiesFromClasspath(String resourceName, ClassLoader classLoader) throws WebzException {

		Properties properties = new Properties();
		loadPropertiesFromClasspath(properties, resourceName, classLoader);

		return properties;
	}

	/** TODO !!! describe !!! **/
	public static void loadPropertiesFromClasspath(Properties properties, String resourceName, ClassLoader classLoader)
			throws WebzException {

		if (resourceName == null) {
			throw new NullPointerException("null resource name was supplied - properties cannot be read");
		}

		try (InputStream in = classLoader.getResourceAsStream(resourceName)) {

			if (in == null) {
				throw new WebzException("'" + resourceName + "' was not found on classpath");
			}
			properties.load(in);

		} catch (IOException e) {
			throw new WebzException("failed to read '" + resourceName + "' from classpath: " + e.getMessage(), e);
		}
	}

	/** TODO !!! describe !!! **/
	public static Properties loadProperties(WebzFile file) throws WebzException {

		Properties properties = new Properties();
		loadProperties(properties, file);

		return properties;
	}

	/** TODO !!! describe !!! **/
	public static void loadProperties(Properties properties, WebzFile file) throws WebzException {

		if (file == null) {
			throw new NullPointerException("null WebzFile was supplied - properties cannot be read");
		}

		WebzFileDownloader fileDownloader = null;
		try {
			fileDownloader = file.getFileDownloader();
			if (fileDownloader == null) {
				throw new WebzException("'" + file.getPathname() + "' was not found");
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

	/** TODO !!! describe !!! **/
	public static String replaceWhitespacesWithDashesSafely(String value) {
		return value == null ? null : value.trim().replaceAll("\\s+", "-");
	}

	/** TODO !!! describe !!! **/
	public static String formatFileSystemMessage(String message, WebzFileSystem fileSystem) {
		return message + " (file system: '" + fileSystem.getFileSystemUniqueId() + "')";
	}

}
