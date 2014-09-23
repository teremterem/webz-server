package org.terems.webz.plugin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzException;
import org.terems.webz.config.StatusCodesConfig;
import org.terems.webz.plugin.base.BaseWebzFilter;

public class NotFoundFilter extends BaseWebzFilter {

	private String pathTo404file;
	private StaticContentSender contentSender;

	@Override
	public void init() throws WebzException {
		pathTo404file = getAppConfig().getAppConfigObject(StatusCodesConfig.class).getPathTo404file();
		contentSender = new StaticContentSender(getAppConfig());
	}

	@Override
	public void serve(HttpServletRequest req, HttpServletResponse resp, WebzChainContext chainContext) throws IOException, WebzException {

		resp.setStatus(HttpServletResponse.SC_NOT_FOUND);

		contentSender.serveStaticContent(resp, chainContext.getFile(pathTo404file));
	}

}