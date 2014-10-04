package org.terems.webz.internals;

import org.terems.webz.WebzDestroyable;
import org.terems.webz.WebzException;
import org.terems.webz.WebzProperties;

public interface WebzFileSystem extends WebzIdentifiable, WebzDestroyable {

	public WebzFileSystem init(WebzProperties properties, WebzDestroyableFactory factory) throws WebzException;

	public WebzFileFactory getFileFactory();

	public WebzPathNormalizer getPathNormalizer();

	public WebzFileSystemStructure getStructure();

	public WebzFileSystemOperations getOperations();

}
