package org.terems.webz.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.DefaultWebzFileRequestResolver;
import org.terems.webz.WebzApp;
import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileFactory;
import org.terems.webz.WebzFileRequestResolver;
import org.terems.webz.WebzFileSystem;
import org.terems.webz.WebzFilterConfig;
import org.terems.webz.WebzResource;
import org.terems.webz.impl.cache.ehcache.EhcacheFileSystemCache;
import org.terems.webz.plugin.WebzFilter;

public class WebzEngine implements WebzApp {

	private static Logger LOG = LoggerFactory.getLogger(WebzEngine.class);

	private WebzFileFactory fileFactory;
	private Collection<WebzFilter> filterChain;

	public WebzEngine(WebzFileSystem fileSystem, Collection<WebzFilter> filterChain) throws IOException, WebzException {
		this.fileFactory = new GenericWebzFileFactory(new EhcacheFileSystemCache(fileSystem));
		this.filterChain = filterChain;

		WebzFilterConfig filterConfig = new WebzFilterConfig() {

			// TODO TODO TODO TODO TODO
			// TODO TODO TODO TODO TODO
			// TODO TODO TODO TODO TODO
			@Override
			public WebzFileFactory fileFactory() {
				return fileFactory;
			}

		};

		for (WebzFilter filter : filterChain) {
			filter.init(filterConfig);
		}
	}

	private static final WebzFileRequestResolver DEFAULT_FILE_REQUEST_RESOLVER = new DefaultWebzFileRequestResolver();

	@Override
	public void service(HttpServletRequest req, HttpServletResponse resp) {

		if (LOG.isTraceEnabled()) {
			LOG.trace("\n\n\n****************************************************************************************************"
					+ "\n***  SERVING " + getFullURL(req)
					+ "\n****************************************************************************************************\n\n");
		}

		try {
			new ChainContext(filterChain.iterator(), DEFAULT_FILE_REQUEST_RESOLVER).nextPlease(req, resp);

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

	private class ChainContext implements WebzChainContext {

		private Iterator<WebzFilter> filterChainIterator;
		private WebzFileRequestResolver fileRequestResolver;

		private HttpServletRequest request;

		public ChainContext(Iterator<WebzFilter> filterChainIterator, WebzFileRequestResolver fileRequestResolver) {
			this.filterChainIterator = filterChainIterator;
			this.fileRequestResolver = fileRequestResolver;
		}

		@Override
		public void nextPlease(HttpServletRequest req, HttpServletResponse resp) throws IOException, WebzException {

			if (filterChainIterator == null) {
				throw new WebzException("WebZ chain is already processed and cannot be invoked again");
			}
			if (filterChainIterator.hasNext()) {

				// remembering request to use it when file request resolver is invoked...
				request = req;

				filterChainIterator.next().service(req, resp, this);

				// invalidating iterator reference to make sure same filters don't invoke the chain for the second time...
				filterChainIterator = null;
			}
		}

		@Override
		public void nextPlease(HttpServletRequest req, HttpServletResponse resp, WebzFileRequestResolver fileRequestResolver)
				throws IOException, WebzException {

			if (fileRequestResolver == this.fileRequestResolver) {
				nextPlease(req, resp);
			} else {
				new ChainContext(filterChainIterator, fileRequestResolver).nextPlease(req, resp);
			}
		}

		@Override
		public WebzFile getRequestedFile() {
			return fileRequestResolver.resolve(fileFactory, request);
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

		@Override
		public WebzFileFactory fileFactory() {
			return fileFactory;
		}
	};

}
