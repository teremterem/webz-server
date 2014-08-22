package org.terems.webz.plugin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzConfig;

/** TODO !!! describe !!! **/
public interface WebzFilter {

	/** TODO !!! describe !!! **/
	public void init(WebzConfig filterConfig) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public void service(HttpServletRequest req, HttpServletResponse resp, WebzChainContext chainContext) throws IOException,
			WebzException;

	/** TODO !!! describe !!! **/
	public void destroy();

}
