/*
 * WebZ Server can serve web pages from various local and remote file sources.
 * Copyright (C) 2013-2015  Oleksandr Tereschenko <http://www.terems.org/>
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
import org.terems.webz.WebzConfig;
import org.terems.webz.WebzContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFilter;
import org.terems.webz.WebzMetadata;
import org.terems.webz.base.WebzContextProxy;
import org.terems.webz.config.GeneralAppConfig;
import org.terems.webz.config.WebzConfigObject;
import org.terems.webz.internals.WebzApp;
import org.terems.webz.internals.WebzDestroyableObjectFactory;
import org.terems.webz.internals.WebzFileFactory;
import org.terems.webz.internals.WebzFileSystem;
import org.terems.webz.util.WebzUtils;

public class GenericWebzApp implements WebzApp {

	private static final Logger LOG = LoggerFactory.getLogger(GenericWebzApp.class);

	private String displayName;

	private RootWebzContext rootContext;
	private Collection<WebzFilter> filterChain;

	private WebzDestroyableObjectFactory appFactory;

	@Override
	public GenericWebzApp init(WebzFileSystem fileSystem, Collection<Class<? extends WebzFilter>> filterClassesList,
			WebzDestroyableObjectFactory appFactory) throws WebzException {

		this.appFactory = appFactory;
		WebzFileFactory fileFactory = fileSystem.getFileFactory();

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
		return this;
	}

	private void initFilterChain(Collection<Class<? extends WebzFilter>> filterClassesList) throws WebzException {

		filterChain = new ArrayList<WebzFilter>(filterClassesList.size());

		for (Class<? extends WebzFilter> filterClass : filterClassesList) {
			filterChain.add(appFactory.newDestroyable(filterClass));
		}

		WebzConfig filterConfig = new WebzConfig() {

			@Override
			public <T extends WebzConfigObject> T getAppConfigObject(Class<T> configObjectClass) throws WebzException {
				return rootContext.getAppConfigObject(configObjectClass);
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
