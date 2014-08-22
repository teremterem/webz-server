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
import org.terems.webz.WebzConfig;
import org.terems.webz.WebzConfigProxy;
import org.terems.webz.WebzContext;
import org.terems.webz.WebzContextProxy;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFileSystem;
import org.terems.webz.impl.cache.ehcache.EhcacheFileSystemCache;
import org.terems.webz.plugin.WebzFilter;

public class WebzEngine implements WebzApp {

	private static Logger LOG = LoggerFactory.getLogger(WebzEngine.class);

	private WebzContext rootContext;
	private Collection<WebzFilter> filterChain;

	public WebzEngine(WebzFileSystem fileSystem, Collection<WebzFilter> filterChain) throws IOException, WebzException {

		// // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\
		this.rootContext = new RootWebzContext(new GenericWebzFileFactory(new EhcacheFileSystemCache(fileSystem)));
		// \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ //

		WebzConfig filterConfig = new WebzConfigProxy() {

			@Override
			protected WebzConfig getInnerConfig() {
				return rootContext;
			}
		};

		this.filterChain = filterChain;

		for (WebzFilter filter : filterChain) {
			filter.init(filterConfig);
		}
	}

	@Override
	public void service(HttpServletRequest req, HttpServletResponse resp) {

		if (LOG.isTraceEnabled()) {
			LOG.trace("\n\n\n// ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\"
					+ "\n SERVING: " + getFullUrl(req)
					+ "\n\\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ //\n\n");
		}

		try {
			new ChainContext(filterChain.iterator(), rootContext).nextPlease(req, resp);

		} catch (IOException | WebzException e) {
			// TODO 500 error page should be displayed to the user instead
			throw new RuntimeException(e);
		}
	}

	private String getFullUrl(HttpServletRequest request) {
		StringBuffer requestUrl = request.getRequestURL();
		String queryString = request.getQueryString();

		if (queryString == null) {
			return requestUrl.toString();
		} else {
			return requestUrl.append('?').append(queryString).toString();
		}
	}

	private static class ChainContext extends WebzContextProxy implements WebzChainContext {

		private Iterator<WebzFilter> filterChainIterator;
		private WebzContext context;

		public ChainContext(Iterator<WebzFilter> filterChainIterator, WebzContext context) {
			this.filterChainIterator = filterChainIterator;
			this.context = context;
		}

		@Override
		public void nextPlease(HttpServletRequest req, HttpServletResponse resp) throws IOException, WebzException {

			if (filterChainIterator == null) {
				throw new WebzException("WebZ chain is already processed and cannot be invoked again");
			}
			if (filterChainIterator.hasNext()) {

				filterChainIterator.next().service(req, resp, this);

				// invalidating iterator reference to make sure same filters don't invoke the chain for the second time...
				filterChainIterator = null;
			}
		}

		@Override
		public void nextPlease(HttpServletRequest req, HttpServletResponse resp, WebzContext contextWrapper) throws IOException,
				WebzException {

			if (contextWrapper == this || contextWrapper == this.context) {
				nextPlease(req, resp);
			} else {
				new ChainContext(filterChainIterator, contextWrapper).nextPlease(req, resp);
			}
		}

		@Override
		protected WebzContext getInnerContext() {
			return context;
		}

	};

}
