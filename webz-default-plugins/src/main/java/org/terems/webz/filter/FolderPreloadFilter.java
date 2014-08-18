package org.terems.webz.filter;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzException;
import org.terems.webz.plugin.BaseWebzFilter;

public class FolderPreloadFilter extends BaseWebzFilter {

	@Override
	public void service(HttpServletRequest req, HttpServletResponse resp, WebzChainContext chainContext) throws IOException, WebzException {

		// preloading "metadata with children"...
		chainContext.getRequestedFile().getChildren();

		chainContext.nextPlease(req, resp);
	}

}
