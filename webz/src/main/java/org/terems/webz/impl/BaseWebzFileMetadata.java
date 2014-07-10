package org.terems.webz.impl;

import java.io.IOException;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFileMetadata;

public abstract class BaseWebzFileMetadata implements WebzFileMetadata, WebzFileMetadata.FileSpecific {

	@Override
	public FileSpecific getFileSpecific() throws IOException, WebzException {
		return isFile() ? this : null;
	}

}
