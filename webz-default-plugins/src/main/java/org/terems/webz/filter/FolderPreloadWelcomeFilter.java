package org.terems.webz.filter;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzException;
import org.terems.webz.plugin.BaseWebzFilter;

public class FolderPreloadWelcomeFilter extends BaseWebzFilter {

	@Override
	public void service(HttpServletRequest req, HttpServletResponse resp, WebzChainContext chainContext) throws IOException, WebzException {

		// TODO TODO TODO

		// TODO to alter the value of req.getPathInfo() in a wrapper or to maintain "current" webz file system path in an
		// isolated manner ?

		// TODO how to redirect to "main" welcome urls ? should this filter be merged with FileFolderRedirectFilter ?

		// preloading "metadata with children"...
		chainContext.resolveFileFromRequest(req).getChildren();

		chainContext.nextPlease(req, resp);
	}

}
