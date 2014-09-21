package org.terems.webz.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFileSystem;

/** TODO !!! describe !!! **/
public class WebzUtils {

	private static final int DEFAULT_BUFFER_SIZE = 8192;

	/** TODO !!! describe !!! **/
	public static long copyInToOut(InputStream in, OutputStream out) throws IOException {

		long bytesTotal = 0;
		int bytes;

		byte[] buff = new byte[DEFAULT_BUFFER_SIZE];

		while ((bytes = in.read(buff)) > 0) {
			out.write(buff, 0, bytes);
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
	public static Properties loadPropertiesFromClasspath(String name) throws WebzException {
		return loadPropertiesFromClasspath(name, DEFAULT_CLASS_LOADER);
	}

	/** TODO !!! describe !!! **/
	public static void loadPropertiesFromClasspath(String name, Properties properties) throws WebzException {
		loadPropertiesFromClasspath(name, properties, DEFAULT_CLASS_LOADER);
	}

	/** TODO !!! describe !!! **/
	public static Properties loadPropertiesFromClasspath(String name, ClassLoader classLoader) throws WebzException {

		Properties properties = new Properties();
		loadPropertiesFromClasspath(name, properties, classLoader);

		return properties;
	}

	/** TODO !!! describe !!! **/
	public static void loadPropertiesFromClasspath(String name, Properties properties, ClassLoader classLoader) throws WebzException {

		if (name == null) {
			throw new WebzException("null resource name was supplied - properties cannot be read");
		}

		InputStream in = classLoader.getResourceAsStream(name);
		if (in == null) {
			throw new WebzException("'" + name + "' was not found on classpath");
		}

		try {
			properties.load(in);

		} catch (IOException e) {
			throw new WebzException("failed to read '" + name + "' from classpath: " + e.getMessage(), e);
		}
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
