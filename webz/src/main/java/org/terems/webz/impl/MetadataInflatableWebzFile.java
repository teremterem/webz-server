package org.terems.webz.impl;

import java.io.IOException;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzMetadata;
import org.terems.webz.base.WebzFileProxy;

public class MetadataInflatableWebzFile extends WebzFileProxy {

	private WebzFile file;

	public MetadataInflatableWebzFile(WebzFile file) {
		this.file = file;
	}

	@Override
	public WebzMetadata getMetadata() throws IOException, WebzException {

		inflate();

		return super.getMetadata();
	}

	@Override
	protected WebzFile getInnerFile() {
		return file;
	}

}
