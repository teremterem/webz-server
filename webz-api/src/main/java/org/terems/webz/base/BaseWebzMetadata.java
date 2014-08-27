package org.terems.webz.base;

import java.io.IOException;

import org.terems.webz.WebzException;
import org.terems.webz.WebzMetadata;

/** TODO !!! describe !!! **/
public abstract class BaseWebzMetadata implements WebzMetadata, WebzMetadata.FileSpecific {

	/** TODO !!! describe !!! **/
	@Override
	public FileSpecific getFileSpecific() throws IOException, WebzException {
		return isFile() ? this : null;
	}

}
