package org.terems.webz;

import java.io.IOException;

/** TODO !!! describe !!! **/
public abstract class BaseWebzMetadata implements WebzMetadata, WebzMetadata.FileSpecific {

	/** TODO !!! describe !!! **/
	@Override
	public FileSpecific getFileSpecific() throws IOException, WebzException {
		return isFile() ? this : null;
	}

}
