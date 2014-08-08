package org.terems.webz.impl;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzEngine;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFileFactory;
import org.terems.webz.WebzFileSystem;
import org.terems.webz.impl.cache.ehcache.EhcacheFileSystemCache;
import org.terems.webz.plugin.WebzFilter;

public class WebzEngineMain implements WebzEngine {

	private static Logger LOG = LoggerFactory.getLogger(WebzEngineMain.class);

	private WebzFileFactory rootFileFactory;
	private WebzFilter rootPlugin;

	// TODO elaborate !!!
	public WebzEngineMain(WebzFileSystem rootFileSystem, WebzFilter rootPlugin) throws IOException, WebzException {
		this.rootFileFactory = new EhcacheFileSystemCache(rootFileSystem);
		this.rootPlugin = rootPlugin;

		this.rootPlugin.init(this.rootFileFactory);
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
			// TODO leverage from "last modified" http logic supported in parent implementation ?

			rootPlugin.service(req, resp);

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
