package org.terems.webz.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzEngine;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFileSystem;
import org.terems.webz.impl.cache.ehcache.EhcacheFileSystemCache;
import org.terems.webz.obsolete.ObsoleteWebzEngine;

public class WebzEngineMain implements WebzEngine {

	private static Logger LOG = LoggerFactory.getLogger(WebzEngineMain.class);

	private ObsoleteWebzEngine obsoleteWebzEngine;

	public WebzEngineMain(WebzFileSystem rootFileSystem) throws WebzException {
		obsoleteWebzEngine = new ObsoleteWebzEngine(new EhcacheFileSystemCache(rootFileSystem));
	}

	@Override
	public void fulfilRequest(HttpServletRequest req, HttpServletResponse resp) {
		if (LOG.isTraceEnabled()) {
			LOG.trace("\n\n\n****************************************************************************************************"
					+ "\n***  SERVING "
					+ getFullURL(req)
					+ "\n****************************************************************************************************\n\n");
		}
		obsoleteWebzEngine.fulfilRequest(req, resp);
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
