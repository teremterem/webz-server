package org.terems.webz.internals;

import org.terems.webz.WebzFile;

/** TODO !!! describe !!! **/
public interface WebzFileFactory {

	/** TODO !!! describe !!! **/
	public WebzFileFactory init(WebzFileSystem fileSystem);

	/** TODO !!! describe !!! **/
	public WebzFile get(String pathname);

}
