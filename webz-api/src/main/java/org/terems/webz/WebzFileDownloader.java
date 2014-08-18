package org.terems.webz;

import java.io.IOException;
import java.io.OutputStream;

/** TODO !!! describe !!! **/
public abstract class WebzFileDownloader {

	/** TODO !!! describe !!! **/
	public WebzFileMetadata.FileSpecific fileSpecific;

	/** TODO !!! describe !!! **/
	public abstract void fileContentToOutputStream(OutputStream out) throws IOException, WebzException;

	// TODO consider exposing input stream as well ?

	/** TODO !!! describe !!! **/
	public WebzFileDownloader(WebzFileMetadata.FileSpecific fileSpecific) {
		this.fileSpecific = fileSpecific;
	}

}
