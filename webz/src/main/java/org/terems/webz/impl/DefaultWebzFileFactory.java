package org.terems.webz.impl;

import org.terems.webz.WebzFile;
import org.terems.webz.base.BaseWebzDestroyable;
import org.terems.webz.internals.WebzFileFactory;
import org.terems.webz.internals.WebzFileSystem;

public class DefaultWebzFileFactory extends BaseWebzDestroyable implements WebzFileFactory {

	// TODO make this factory a first level cache ? (for the sake of file inflation concept)

	private WebzFileSystem fileSystem;

	public DefaultWebzFileFactory(WebzFileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}

	@Override
	public WebzFile get(String pathname) {
		return new MetadataInflatableWebzFile(pathname, fileSystem);
	}

}
