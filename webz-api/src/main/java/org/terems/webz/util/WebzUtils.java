package org.terems.webz.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
	public static String formatFileSystemMessage(String message, WebzFileSystem fileSystem) {
		return message + " (file system: '" + fileSystem.getFileSystemUniqueId() + "')";
	}

}
