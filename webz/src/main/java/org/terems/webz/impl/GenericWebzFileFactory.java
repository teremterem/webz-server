package org.terems.webz.impl;

import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileFactory;
import org.terems.webz.WebzFileSystem;

public class GenericWebzFileFactory implements WebzFileFactory {

	// TODO refactor this method ?
	@Deprecated
	public static String trimFileSeparators(String pathName) {
		pathName = pathName.trim();
		if (pathName.startsWith("/") || pathName.startsWith("\\")) {
			pathName = pathName.substring(1);
		}
		if (pathName.endsWith("/") || pathName.endsWith("\\")) {
			pathName = pathName.substring(0, pathName.length() - 1);
		}
		return pathName;
	}

	private WebzFileSystem fileSystem;

	public GenericWebzFileFactory(WebzFileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}

	@Override
	public WebzFile get(String pathInfo) {
		// TODO revise path normalization logic
		// TODO + force 404(?) for cases like http://localhost:8080//////webz-pedesis.html
		return new GenericWebzFile(pathInfo == null ? "" : trimFileSeparators(pathInfo), fileSystem);
	}

}
