package org.terems.webz.filter;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzException;
import org.terems.webz.plugin.base.BaseWebzFilter;

public class NotFoundFilter extends BaseWebzFilter {

	// TODO move it to config folder
	private String pathTo404html = "404.html";

	@Override
	public void serve(HttpServletRequest req, HttpServletResponse resp, WebzChainContext chainContext) throws IOException, WebzException {

		resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		chainContext.getFile(pathTo404html).copyContentToOutputStream(resp.getOutputStream());
	}

}
