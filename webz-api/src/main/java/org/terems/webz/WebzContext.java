package org.terems.webz;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

/** TODO !!! describe !!! **/
public interface WebzContext extends WebzConfig {

	/** TODO !!! describe !!! **/
	public WebzFile resolveFile(HttpServletRequest req) throws IOException, WebzException;

}
