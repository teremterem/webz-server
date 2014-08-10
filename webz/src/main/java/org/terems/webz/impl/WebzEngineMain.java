package org.terems.webz.impl;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzEngine;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFileFactory;
import org.terems.webz.WebzFileSystem;
import org.terems.webz.WebzFilterConfig;
import org.terems.webz.WebzResource;
import org.terems.webz.impl.cache.ehcache.EhcacheFileSystemCache;
import org.terems.webz.plugin.WebzFilter;

public class WebzEngineMain implements WebzEngine {

	private static Logger LOG = LoggerFactory.getLogger(WebzEngineMain.class);

	private WebzFileFactory rootFileFactory;
	private WebzFilter rootPlugin;

	private WebzChainContext chainContext = new WebzChainContext() {

		@Override
		public WebzFileFactory fileFactory() {
			return rootFileFactory;
		}

		@Override
		public void nextPlease(HttpServletRequest req, HttpServletResponse resp) throws IOException, WebzException {
			// TODO TODO TODO TODO TODO
			// TODO TODO TODO TODO TODO
			// TODO TODO TODO TODO TODO
			// TODO TODO TODO TODO TODO
			// TODO TODO TODO TODO TODO
		}

		@Override
		public WebzResource webzGet(String uriORurl) {
			// TODO TODO TODO TODO TODO
			// TODO TODO TODO TODO TODO
			// TODO TODO TODO TODO TODO
			// TODO TODO TODO TODO TODO
			// TODO TODO TODO TODO TODO
			return null;
		}
	};

	// TODO elaborate !!!
	public WebzEngineMain(WebzFileSystem rootFileSystem, WebzFilter rootPlugin) throws IOException, WebzException {
		this.rootFileFactory = new EhcacheFileSystemCache(rootFileSystem);
		this.rootPlugin = rootPlugin;

		this.rootPlugin.init(new WebzFilterConfig() {
			// TODO TODO TODO TODO TODO
			// TODO TODO TODO TODO TODO
			// TODO TODO TODO TODO TODO
			// TODO TODO TODO TODO TODO
			// TODO TODO TODO TODO TODO
		});
	}

	@Override
	public void service(HttpServletRequest req, HttpServletResponse resp) {

		if (LOG.isTraceEnabled()) {
			LOG.trace("\n\n\n****************************************************************************************************"
					+ "\n***  SERVING "
					+ getFullURL(req)
					+ "\n****************************************************************************************************\n\n");
		}
		try {
			// TODO TODO TODO TODO TODO TODO TODO TODO
			// TODO TODO TODO TODO TODO TODO TODO TODO
			// TODO TODO TODO TODO TODO TODO TODO TODO
			// TODO TODO TODO TODO TODO TODO TODO TODO
			// TODO TODO TODO TODO TODO TODO TODO TODO
			// TODO support last modified concept TODO
			// TODO TODO TODO TODO TODO TODO TODO TODO
			// TODO TODO TODO TODO TODO TODO TODO TODO
			// TODO TODO TODO TODO TODO TODO TODO TODO
			// TODO TODO TODO TODO TODO TODO TODO TODO
			// TODO TODO TODO TODO TODO TODO TODO TODO

			rootPlugin.service(req, resp, chainContext);

		} catch (IOException | WebzException e) {
			// TODO 500 error page should be displayed to the user instead
			throw new RuntimeException(e);
		}
	}

	private String getFullURL(HttpServletRequest request) {
		StringBuffer requestURL = request.getRequestURL();
		String queryString = request.getQueryString();

		if (queryString == null) {
			return requestURL.toString();
		} else {
			return requestURL.append('?').append(queryString).toString();
		}
	}

}
