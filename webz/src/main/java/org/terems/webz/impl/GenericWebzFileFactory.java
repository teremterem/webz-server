package org.terems.webz.impl;

import org.terems.webz.GenericWebzFile;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileFactory;
import org.terems.webz.WebzFileSystem;

public class GenericWebzFileFactory implements WebzFileFactory {

	private WebzFileSystem fileSystem;

	public GenericWebzFileFactory(WebzFileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}

	@Override
	public WebzFile get(String pathInfo) {
		// TODO revise path normalization logic
		// TODO + force 404(?) for cases like http://localhost:8080//////webz-pedesis.html
		return new GenericWebzFile(pathInfo == null ? "" : GenericWebzFile.trimFileSeparators(pathInfo), fileSystem);
	}

}
