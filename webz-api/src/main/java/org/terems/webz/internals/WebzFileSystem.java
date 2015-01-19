package org.terems.webz.internals;

import org.terems.webz.WebzDestroyable;
import org.terems.webz.WebzException;
import org.terems.webz.WebzProperties;

public interface WebzFileSystem extends WebzIdentifiable, WebzDestroyable {

	// TODO elaborate a concept of WebzFileAccessController (in terms of servlet container it should be user session scoped)

	public WebzFileSystem init(WebzObjectFactory factory, WebzProperties properties) throws WebzException;

	public WebzFileFactory getFileFactory();

	public WebzPathNormalizer getPathNormalizer();

	public WebzFileSystemStructure getStructure();

	public WebzFileSystemOperations getOperations();

	// TODO should any kind of optimistic locking be supported when files are edited through "operations" ?

}
