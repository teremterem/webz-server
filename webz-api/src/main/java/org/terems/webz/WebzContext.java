package org.terems.webz;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

/** TODO !!! describe !!! **/
public interface WebzContext {

	/** TODO !!! describe !!! **/
	public WebzFile resolveFile(HttpServletRequest req) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzFile getFile(String pathInfo) throws IOException, WebzException;

}
