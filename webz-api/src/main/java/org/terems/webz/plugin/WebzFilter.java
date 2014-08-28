package org.terems.webz.plugin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzConfig;
import org.terems.webz.WebzDestroyable;
import org.terems.webz.WebzException;

/** TODO !!! describe !!! **/
public interface WebzFilter extends WebzDestroyable {

	/** TODO !!! describe !!! **/
	public void init(WebzConfig appConfig) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public void serve(HttpServletRequest req, HttpServletResponse resp, WebzChainContext chainContext) throws IOException, WebzException;

}
