package org.terems.webz.impl;

import java.io.IOException;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFileFactory;
import org.terems.webz.WebzFileSystem;
import org.terems.webz.WebzMetadata;

public class MetadataInflatableWebzFile extends GenericWebzFile {

	public MetadataInflatableWebzFile(String pathname, WebzFileFactory fileFactory, WebzFileSystem fileSystem) {
		super(pathname, fileFactory, fileSystem);
	}

	@Override
	public WebzMetadata getMetadata() throws IOException, WebzException {

		inflate();

		return super.getMetadata();
	}

}
