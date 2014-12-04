package org.terems.webz.internals;

import org.terems.webz.WebzFile;
import org.terems.webz.WebzProperties;

/** TODO !!! describe !!! **/
public interface WebzFileFactory {

	/** TODO !!! describe !!! **/
	public WebzFileFactory init(WebzFileSystem fileSystem, WebzProperties properties);

	/** TODO !!! describe !!! **/
	public WebzFile get(String pathname);

}
