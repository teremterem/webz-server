package org.terems.webz.impl;

import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileFactory;
import org.terems.webz.WebzFileSystem;

public class DefaultWebzFileFactory implements WebzFileFactory {

	private WebzFileSystem fileSystem;

	public DefaultWebzFileFactory(WebzFileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}

	@Override
	public WebzFile get(String pathName) {
		return new MetadataInflatableWebzFile(fileSystem.normalizePathName(pathName), this, fileSystem);
	}

}
