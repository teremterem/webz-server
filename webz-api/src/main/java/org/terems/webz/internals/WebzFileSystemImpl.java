package org.terems.webz.internals;

import org.terems.webz.WebzDestroyable;
import org.terems.webz.WebzException;
import org.terems.webz.WebzProperties;

/** TODO !!! describe !!! **/
public interface WebzFileSystemImpl extends WebzFileSystemStructure, WebzFileSystemOperations, WebzIdentifiable, WebzDestroyable {

	/** TODO !!! describe !!! **/
	public void init(WebzPathNormalizer pathNormalizer, WebzProperties properties, WebzObjectFactory factory) throws WebzException;

}
