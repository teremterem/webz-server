package org.terems.webz.impl;

import org.terems.webz.GenericWebzFile;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileFactory;
import org.terems.webz.WebzFileSystem;

public class GenericFileFactory implements WebzFileFactory {

	private WebzFileSystem fileSystem;

	public GenericFileFactory(WebzFileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}

	@Override
	public WebzFile get(String pathName) {
		// TODO revise path normalization logic
		// TODO + force 404(?) for cases like http://localhost:8080//////webz-pedesis.html
		return new GenericWebzFile(pathName == null ? "" : GenericWebzFile.trimFileSeparators(pathName), fileSystem);
	}

}
