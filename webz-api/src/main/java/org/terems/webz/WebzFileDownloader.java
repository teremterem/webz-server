package org.terems.webz;

import java.io.InputStream;
import java.io.OutputStream;

import org.terems.webz.util.WebzUtils;

/** TODO !!! describe !!! **/
public class WebzFileDownloader {

	/** TODO !!! describe !!! **/
	public final WebzMetadata.FileSpecific fileSpecific;

	/** TODO !!! describe !!! **/
	public final InputStream content;

	/** TODO !!! describe !!! **/
	public long copyContentAndClose(OutputStream out) throws WebzReadException, WebzWriteException {

		try {
			return WebzUtils.copyInToOut(content, out);
		} finally {
			close();
		}
	}

	/** TODO !!! describe !!! **/
	public void close() {
		WebzUtils.closeSafely(content);
	}

	/** TODO !!! describe !!! **/
	public WebzFileDownloader(WebzMetadata.FileSpecific fileSpecific, InputStream content) {
		this.fileSpecific = fileSpecific;
		this.content = content;
	}

}
