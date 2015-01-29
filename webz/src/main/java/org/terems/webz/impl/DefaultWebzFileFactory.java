package org.terems.webz.impl;

import org.terems.webz.WebzDefaults;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzProperties;
import org.terems.webz.base.BaseWebzDestroyable;
import org.terems.webz.internals.WebzFileFactory;
import org.terems.webz.internals.WebzFileSystem;

public class DefaultWebzFileFactory extends BaseWebzDestroyable implements WebzFileFactory {

	// TODO make this factory a first level cache ? (for the sake of file "inflation" concept)

	private WebzFileSystem fileSystem;
	private boolean useMetadataInflatableFiles;

	@Override
	public DefaultWebzFileFactory init(WebzFileSystem fileSystem, WebzProperties properties) {

		this.fileSystem = fileSystem;
		this.useMetadataInflatableFiles = Boolean.valueOf(properties.get(WebzProperties.USE_METADATA_INFLATABLE_FILES_PROPERTY,
				String.valueOf(WebzDefaults.USE_METADATA_INFLATABLE_FILES)));
		return this;
	}

	@Override
	public WebzFile get(String pathname) {
		return useMetadataInflatableFiles ? new MetadataInflatableWebzFile(pathname, fileSystem)
				: new GenericWebzFile(pathname, fileSystem);
	}

}
