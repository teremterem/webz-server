package org.terems.webz.plugin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFilterConfig;

/** TODO !!! describe !!! **/
public interface WebzFilter {

	/** TODO !!! describe !!! **/
	public void init(WebzFilterConfig filterConfig) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public void service(HttpServletRequest req, HttpServletResponse resp, WebzChainContext chainContext) throws IOException,
			WebzException;

	/** TODO !!! describe !!! **/
	public long getLastModified(HttpServletRequest req);

	/** TODO !!! describe !!! **/
	public void destroy();

}
