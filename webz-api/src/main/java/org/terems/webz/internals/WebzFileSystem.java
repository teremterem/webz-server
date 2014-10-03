package org.terems.webz.internals;

import org.terems.webz.WebzDestroyable;
import org.terems.webz.WebzIdentifiable;
import org.terems.webz.WebzPropertiesInitable;

public interface WebzFileSystem extends WebzPropertiesInitable, WebzIdentifiable, WebzDestroyable {

	public WebzFileFactory getFileFactory();

	public WebzPathNormalizer getPathNormalizer();

	public WebzFileSystemStructure getStructure();

	public WebzFileSystemOperations getOperations();

}
