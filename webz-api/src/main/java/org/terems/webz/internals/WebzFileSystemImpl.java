package org.terems.webz.internals;

import org.terems.webz.WebzException;
import org.terems.webz.WebzIdentifiable;
import org.terems.webz.WebzProperties;
import org.terems.webz.WebzPropertiesInitable;

/** TODO !!! describe !!! **/
public interface WebzFileSystemImpl extends WebzFileSystemStructure, WebzFileSystemOperations, WebzPropertiesInitable, WebzIdentifiable {

	/** TODO !!! describe !!! **/
	public WebzFileSystemImpl init(WebzPathNormalizer pathNormalizer, WebzProperties properties) throws WebzException;

}
