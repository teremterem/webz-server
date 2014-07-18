package org.terems.webz.internal;

import java.io.IOException;
import java.io.OutputStream;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFileMetadata;

public abstract class WebzFileDownloader {

	public WebzFileMetadata metadata;

	public abstract void fileContentToOutputStream(OutputStream out) throws IOException, WebzException;

	public WebzFileDownloader(WebzFileMetadata metadata) {
		this.metadata = metadata;
	}

}
