package org.terems.webz;

import java.io.IOException;
import java.io.OutputStream;

/** TODO !!! describe !!! **/
public abstract class WebzFileDownloader {

	/** TODO !!! describe !!! **/
	public WebzFileMetadata metadata;

	/** TODO !!! describe !!! **/
	public abstract void fileContentToOutputStream(OutputStream out) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzFileDownloader(WebzFileMetadata metadata) {
		this.metadata = metadata;
	}

}
