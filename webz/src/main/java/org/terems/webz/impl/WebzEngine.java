package org.terems.webz.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzApp;
import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFileFactory;
import org.terems.webz.WebzFileSystem;
import org.terems.webz.WebzFilterConfig;
import org.terems.webz.WebzResource;
import org.terems.webz.impl.cache.ehcache.EhcacheFileSystemCache;
import org.terems.webz.plugin.WebzFilter;

public class WebzEngine implements WebzApp {

	private static Logger LOG = LoggerFactory.getLogger(WebzEngine.class);

	private WebzFileFactory rootFileFactory;
	private Collection<WebzFilter> filterChain;

	public WebzEngine(WebzFileSystem rootFileSystem, Collection<WebzFilter> filterChain) throws IOException, WebzException {
		this.rootFileFactory = new EhcacheFileSystemCache(rootFileSystem);
		this.filterChain = filterChain;

		WebzFilterConfig filterConfig = new WebzFilterConfig() {

			@Override
			public WebzFileFactory fileFactory() {
				return rootFileFactory;
			}

			// TODO TODO TODO TODO TODO
			// TODO TODO TODO TODO TODO
			// TODO TODO TODO TODO TODO

		};

		for (WebzFilter filter : filterChain) {
			filter.init(filterConfig);
		}
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
			WebzChainContext chainContext = new WebzChainContext() {

				private Iterator<WebzFilter> filterChainIterator = filterChain.iterator();

				@Override
				public WebzFileFactory fileFactory() {
					return rootFileFactory;
				}

				@Override
				public void nextPlease(HttpServletRequest req, HttpServletResponse resp) throws IOException, WebzException {

					if (filterChainIterator.hasNext()) {
						filterChainIterator.next().service(req, resp, this);
					}
				}

				// TODO reusable request wrapper in ThreadLocal

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

			chainContext.nextPlease(req, resp);

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
