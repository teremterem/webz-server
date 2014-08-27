package org.terems.webz.filter;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzConfig;
import org.terems.webz.WebzException;
import org.terems.webz.plugin.WebzFilter;

public class NotFoundFilter implements WebzFilter {

	// TODO move it to config folder
	private String pathTo404html = "404.html";

	@Override
	public void service(HttpServletRequest req, HttpServletResponse resp, WebzChainContext chainContext) throws IOException, WebzException {

		resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		chainContext.getFile(pathTo404html).fileContentToOutputStream(resp.getOutputStream());
	}

	@Override
	public void init(WebzConfig appConfig) throws IOException, WebzException {
	}

	@Override
	public void destroy() {
	}

}
