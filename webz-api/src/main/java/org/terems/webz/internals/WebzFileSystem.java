package org.terems.webz.internals;

import org.terems.webz.WebzDestroyable;
import org.terems.webz.WebzException;
import org.terems.webz.WebzProperties;

public interface WebzFileSystem extends WebzIdentifiable, WebzDestroyable {

	// TODO elaborate a concept of WebzFileAccessController (in terms of servlet container it should be user session scoped)

	public WebzFileSystem init(WebzProperties properties, WebzObjectFactory factory) throws WebzException;

	public WebzFileFactory getFileFactory();

	public WebzPathNormalizer getPathNormalizer();

	public WebzFileSystemStructure getStructure();

	public WebzFileSystemOperations getOperations();

	// TODO any ideas on file blocking/merging approaches when they are edited through WebZ ?

}
