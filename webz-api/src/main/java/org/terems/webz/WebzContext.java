package org.terems.webz;

import javax.servlet.http.HttpServletRequest;

/** TODO !!! describe !!! **/
public interface WebzContext {

	/** TODO !!! describe !!! **/
	public WebzFile resolveFileFromRequest(HttpServletRequest req);

	/** TODO !!! describe !!! **/
	public WebzFile resolveFile(String pathInfo);

}
