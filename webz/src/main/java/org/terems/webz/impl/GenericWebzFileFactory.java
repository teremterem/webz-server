package org.terems.webz.impl;

import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileFactory;
import org.terems.webz.WebzFileSystem;

public class GenericWebzFileFactory implements WebzFileFactory {

	private WebzFileSystem fileSystem;

	public GenericWebzFileFactory(WebzFileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}

	@Override
	public WebzFile get(String pathName) {
		return new GenericWebzFile(pathName, this, fileSystem);
	}

}
