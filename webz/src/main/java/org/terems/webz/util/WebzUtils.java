package org.terems.webz.util;

import org.slf4j.Logger;
import org.terems.webz.WebzFileSystem;

public class WebzUtils {

	public static void traceFSMessage(Logger log, String message, WebzFileSystem fileSystem) {
		log.trace(message + " (file system: '" + fileSystem.getFileSystemUniqueId() + "')");
	}

}
