package org.terems.webz;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.terems.webz.util.WebzUtils;

/** TODO !!! describe !!! **/
public class WebzFileDownloader {

	/** TODO !!! describe !!! **/
	public WebzMetadata.FileSpecific fileSpecific;

	/** TODO !!! describe !!! **/
	public InputStream content;

	/** TODO !!! describe !!! **/
	public long copyContentAndClose(OutputStream out) throws IOException {

		try {
			return copyContent(out);
		} finally {
			close();
		}
	}

	/** TODO !!! describe !!! **/
	public void close() {
		WebzUtils.closeSafely(content);
	}

	/** Defined as a separate method for more convenient overriding... **/
	protected long copyContent(OutputStream out) throws IOException {
		return WebzUtils.copyInToOut(content, out);
	}

	/** TODO !!! describe !!! **/
	public WebzFileDownloader(WebzMetadata.FileSpecific fileSpecific, InputStream content) {
		this.fileSpecific = fileSpecific;
		this.content = content;
	}

}
