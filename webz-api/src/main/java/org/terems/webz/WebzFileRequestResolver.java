package org.terems.webz;

import javax.servlet.http.HttpServletRequest;

/** TODO !!! describe !!! **/
public interface WebzFileRequestResolver {

	/** TODO !!! describe !!! **/
	public WebzFile resolve(WebzFileFactory fileFactory, HttpServletRequest req);

}
