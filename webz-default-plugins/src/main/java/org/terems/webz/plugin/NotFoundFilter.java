package org.terems.webz.plugin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzException;
import org.terems.webz.base.BaseWebzFilter;
import org.terems.webz.config.StatusCodesConfig;

public class NotFoundFilter extends BaseWebzFilter {

	private String pathTo404file;
	private StaticContentSender contentSender;

	@Override
	public void init() throws WebzException {
		pathTo404file = getAppConfig().getAppConfigObject(StatusCodesConfig.class).getPathTo404file();
		contentSender = new StaticContentSender(getAppConfig());
		// TODO convert StaticContentSender into a singleton
	}

	@Override
	public void serve(HttpServletRequest req, HttpServletResponse resp, WebzChainContext chainContext) throws IOException, WebzException {

		resp.setStatus(HttpServletResponse.SC_NOT_FOUND);

		if (pathTo404file != null) {
			contentSender.serveStaticContent(req, resp, chainContext.getFile(pathTo404file));
		}
	}

}
