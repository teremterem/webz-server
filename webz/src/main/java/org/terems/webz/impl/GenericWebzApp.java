package org.terems.webz.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzApp;
import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzConfig;
import org.terems.webz.WebzContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFileFactory;
import org.terems.webz.WebzFileSystem;
import org.terems.webz.WebzMetadata;
import org.terems.webz.base.WebzConfigProxy;
import org.terems.webz.base.WebzContextProxy;
import org.terems.webz.config.GeneralAppConfig;
import org.terems.webz.plugin.WebzFilter;
import org.terems.webz.util.WebzUtils;

public class GenericWebzApp implements WebzApp {

	private static final Logger LOG = LoggerFactory.getLogger(GenericWebzApp.class);

	private String displayName;

	private RootWebzContext rootContext;
	private Collection<WebzFilter> filterChain;

	private WebzDestroyableFactory appFactory = new WebzDestroyableFactory();

	@Override
	public void init(WebzFileSystem fileSystem, Collection<Class<? extends WebzFilter>> filterClassesList) throws WebzException {

		WebzFileFactory fileFactory = new DefaultWebzFileFactory(fileSystem);

		// // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ //
		rootContext = new RootWebzContext(fileFactory, appFactory);
		// \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\

		try {
			WebzMetadata rootMetadata = fileFactory.get("").getMetadata();

			if (rootMetadata == null) {
				throw new WebzException(WebzUtils.formatFileSystemMessage("failed to initialize WebZ App - root location does not exist",
						fileSystem));
			}

			displayName = rootContext.getAppConfigObject(GeneralAppConfig.class).getAppDisplayName();
			if (displayName == null) {
				displayName = rootMetadata.getName();
			}

		} catch (IOException e) {
			throw new WebzException(e);
		}

		initFilterChain(filterClassesList);

		if (LOG.isInfoEnabled()) {
			LOG.info("WebZ App '" + displayName + "' initialized");
		}
	}

	private void initFilterChain(Collection<Class<? extends WebzFilter>> filterClassesList) throws WebzException {

		filterChain = new ArrayList<>(filterClassesList.size());

		for (Class<? extends WebzFilter> filterClass : filterClassesList) {
			filterChain.add(appFactory.newDestroyable(filterClass));
		}

		WebzConfig filterConfig = new WebzConfigProxy() {

			@Override
			protected WebzConfig getInnerConfig() {
				return rootContext;
			}
		};

		for (WebzFilter filter : filterChain) {
			filter.init(filterConfig);
		}
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public void serve(HttpServletRequest req, HttpServletResponse resp) throws IOException, WebzException {

		if (LOG.isTraceEnabled()) {
			LOG.trace("\n\n\n// ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\\n "
					+ req.getMethod() + " " + getFullUrl(req)
					+ "\n\\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ //\n\n");
		}

		new ChainContext(filterChain.iterator(), rootContext).nextPlease(req, resp);
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

				// TODO <fileMask /> !

				filterChainIterator.next().serve(req, resp, this);

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
	}

	@Override
	public void destroy() {

		appFactory.destroy();

		if (LOG.isInfoEnabled()) {
			LOG.info("WebZ App '" + displayName + "' destroyed");
		}
	}

	@Override
	public String toString() {
		return displayName + " - " + super.toString();
	}

}
