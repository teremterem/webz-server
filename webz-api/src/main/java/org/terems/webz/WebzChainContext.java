package org.terems.webz;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** TODO !!! describe !!! **/
public interface WebzChainContext extends WebzContext {

	/** TODO !!! describe !!! **/
	public void nextPlease(HttpServletRequest req, HttpServletResponse resp) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public void nextPlease(HttpServletRequest req, HttpServletResponse resp, WebzFileFactory fileFactoryWrapper) throws IOException,
			WebzException;

}
