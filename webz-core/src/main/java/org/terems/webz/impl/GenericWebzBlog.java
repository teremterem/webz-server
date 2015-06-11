/*
 * WebZ Server can serve web pages from various local and remote file sources.
 * Copyright (C) 2014-2015  Oleksandr Tereschenko <http://www.terems.org/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.terems.webz.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFilter;
import org.terems.webz.WebzMetadata;
import org.terems.webz.base.WebzContextProxy;
import org.terems.webz.config.GeneralAppConfig;
import org.terems.webz.internals.RootWebzContext;
import org.terems.webz.internals.WebzApp;
import org.terems.webz.internals.WebzDestroyableObjectFactory;
import org.terems.webz.internals.WebzFileFactory;
import org.terems.webz.internals.WebzFileSystem;
import org.terems.webz.util.WebzUtils;

public class GenericWebzBlog implements WebzApp {

	private static final Logger LOG = LoggerFactory.getLogger(GenericWebzBlog.class);

	private String displayName;

	private RootWebzContext rootContext;
	private Collection<WebzFilter> filterChain;

	private WebzDestroyableObjectFactory appFactory;

	@Override
	public GenericWebzBlog init(WebzFileSystem fileSystem, Collection<Class<? extends WebzFilter>> filterClassesList,
			WebzDestroyableObjectFactory appFactory) throws WebzException {

		this.appFactory = appFactory;
		WebzFileFactory fileFactory = fileSystem.getFileFactory();

		try {
			WebzMetadata rootMetadata = fileFactory.get("").getMetadata();

			if (rootMetadata == null) {
				throw new WebzException(WebzUtils.formatFileSystemMessage("root location does not exist", fileSystem));
			}
			this.displayName = rootMetadata.getName();

			if (!rootMetadata.isFolder()) {
				throw new WebzException(WebzUtils.formatFileSystemMessage("root location is not a folder", fileSystem));
			}

			// // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ //
			this.rootContext = new RootWebzContext(fileSystem.getFileFactory(), appFactory);
			// \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\

			GeneralAppConfig appConfig = new GeneralAppConfig();
			appConfig.init(rootContext.getConfigFolder());

			// // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ //
			// TODO move app encoding property to deployment level:
			// \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\
			fileSystem.setDefaultEncoding(appConfig.getDefaultEncoding());

			appConfig.destroy();
			appConfig = rootContext.getConfigObject(GeneralAppConfig.class);

			String configuredDisplayName = appConfig.getAppDisplayName();
			if (configuredDisplayName != null) {

				this.displayName = configuredDisplayName;
			}

			initFilterChain(filterClassesList);

		} catch (IOException e) {
			throw new WebzException(e);
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("WebZ Blog \"" + displayName + "\" deployed");
		}
		return this;
	}

	private void initFilterChain(Collection<Class<? extends WebzFilter>> filterClassesList) throws IOException, WebzException {

		filterChain = new ArrayList<WebzFilter>(filterClassesList.size());

		for (Class<? extends WebzFilter> filterClass : filterClassesList) {
			filterChain.add(appFactory.newDestroyable(filterClass));
		}
		for (WebzFilter filter : filterChain) {
			filter.init(rootContext, rootContext);
		}
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public void serve(HttpServletRequest req, HttpServletResponse resp) throws IOException, WebzException {

		new ChainContext(filterChain.iterator(), rootContext).nextPlease(req, resp);
	}

	private static class ChainContext extends WebzContextProxy implements WebzChainContext {

		private Iterator<WebzFilter> filterChainIterator;
		private WebzContext context;

		public ChainContext(Iterator<WebzFilter> filterChainIterator, WebzContext context) {
			this.filterChainIterator = filterChainIterator;
			this.context = context;
		}

		@Override
		public void nextPlease(HttpServletRequest req, HttpServletResponse resp, Class<? extends WebzFilter> nextFilter)
				throws IOException, WebzException {

			if (filterChainIterator == null) {
				throw new WebzException("WebZ filter chain is already processed and cannot be invoked again");
			}
			if (filterChainIterator.hasNext()) {

				WebzFilter next = filterChainIterator.next();
				if (nextFilter != null) {

					while (!nextFilter.isAssignableFrom(next.getClass())) {

						if (!filterChainIterator.hasNext()) {
							next = null;
							break;
						}
						next = filterChainIterator.next();
					}
				}
				if (next != null) {
					next.serve(req, resp, this);
				}

				// invalidating iterator reference to make sure same filters don't invoke the chain for the second time...
				filterChainIterator = null;
			}
		}

		@Override
		public void nextPlease(HttpServletRequest req, HttpServletResponse resp, WebzContext contextWrapper,
				Class<? extends WebzFilter> nextFilter) throws IOException, WebzException {

			if (contextWrapper == this || contextWrapper == this.context) {
				nextPlease(req, resp, nextFilter);
			} else {
				new ChainContext(filterChainIterator, contextWrapper).nextPlease(req, resp, nextFilter);
			}
		}

		@Override
		protected WebzContext getInternalContext() {
			return context;
		}

		@Override
		public void nextPlease(HttpServletRequest req, HttpServletResponse resp) throws IOException, WebzException {
			nextPlease(req, resp, (Class<? extends WebzFilter>) null);
		}

		@Override
		public void nextPlease(HttpServletRequest req, HttpServletResponse resp, WebzContext contextWrapper) throws IOException,
				WebzException {
			nextPlease(req, resp, contextWrapper, (Class<? extends WebzFilter>) null);
		}

	}

	@Override
	public void destroy() {

		appFactory.destroy();

		if (LOG.isInfoEnabled()) {
			LOG.info("WebZ Blog \"" + displayName + "\" destroyed");
		}
	}

	@Override
	public String toString() {
		return displayName + " - " + super.toString();
	}

}
