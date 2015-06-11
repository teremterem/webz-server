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
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFilter;
import org.terems.webz.WebzProperties;
import org.terems.webz.filter.ErrorFilter;
import org.terems.webz.filter.ForcedRedirectsFilter;
import org.terems.webz.filter.JavascriptEngineFilter;
import org.terems.webz.filter.NotFoundFilter;
import org.terems.webz.filter.StaticContentFilter;
import org.terems.webz.filter.WelcomeFilter;
import org.terems.webz.internals.WebzApp;
import org.terems.webz.internals.WebzDestroyableObjectFactory;
import org.terems.webz.internals.WebzFileSystem;
import org.terems.webz.internals.WebzNode;
import org.terems.webz.util.WebzUtils;

public class WebzServer implements WebzNode {

	private static final Logger LOG = LoggerFactory.getLogger(WebzServer.class);

	@SuppressWarnings("unchecked")
	private static final Collection<Class<? extends WebzFilter>> DEFAULT_FILTERS = Arrays
			.asList((Class<? extends WebzFilter>[]) new Class<?>[] { ErrorFilter.class, WelcomeFilter.class, ForcedRedirectsFilter.class,
					JavascriptEngineFilter.class, StaticContentFilter.class, NotFoundFilter.class });

	// TODO introduce ServiceUrlsFilter

	// TODO implement FileMaskFilterMapping as a WebzFilter that leverages from chainContext.nextPlease(..., nextFilter) methods

	private WebzDestroyableObjectFactory globalFactory = new GenericWebzObjectFactory();
	private volatile WebzApp rootWebzApp;

	/**
	 * @return by default returns {@code null} which means serve from any hostnames
	 */
	@Override
	public Set<String> getHostNamesLowerCased() {

		// TODO support configurable domain names
		return null;
	}

	@Override
	public void serve(HttpServletRequest req, HttpServletResponse resp) throws IOException, WebzException {

		if (rootWebzApp == null) {
			throw new WebzException("root WebZ App is already destroyed or has not been deployed yet");
		}

		traceRequestStart(req);

		rootWebzApp.serve(req, resp);

		traceRequestEnd(req, resp);
	}

	public static WebzServer start(WebzProperties siteFileSystemProperties, WebzProperties spaFileSystemProperties,
			WebzProperties webzBoilerplateProperties, WebzProperties hybridFileSystemProperties) {

		// // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ //
		// TODO !!! implement configurable WebZ Deployment(s) !!!
		// \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\

		WebzServer webzServer = new WebzServer();

		try {
			WebzFileSystemManager fileSystemManager = WebzFileSystemManager.getManager(webzServer.globalFactory);
			GenericWebzObjectFactory appFactory = webzServer.globalFactory.newDestroyable(GenericWebzObjectFactory.class);

			WebzFileSystem siteFileSystem = fileSystemManager.createFileSystem(siteFileSystemProperties);
			WebzFileSystem spaFileSystem = fileSystemManager.createFileSystem(spaFileSystemProperties);
			WebzFileSystem boilerplateFileSystem = fileSystemManager.createFileSystem(webzBoilerplateProperties);

			// TODO decide whether WebZ Boilerplate File System needs it's own Origin Name or not
			WebzFileSystem spaAndBoilerplateOverlay = fileSystemManager.createSimpleFileSystemOverlay(spaFileSystem,
					WebzFilter.FILE_ORIGIN_SPA, boilerplateFileSystem, WebzFilter.FILE_ORIGIN_SPA, hybridFileSystemProperties);
			WebzFileSystem siteSpaAndBoilerplateOverlay = fileSystemManager.createSiteAndSpaFileSystem(siteFileSystem,
					spaAndBoilerplateOverlay, hybridFileSystemProperties);

			webzServer.rootWebzApp = webzServer.globalFactory.newDestroyable(WebzBlog.class);
			webzServer.rootWebzApp.init(siteSpaAndBoilerplateOverlay, DEFAULT_FILTERS, appFactory);

		} catch (WebzException e) {

			webzServer.rootWebzApp = null;
			if (LOG.isErrorEnabled()) {
				LOG.error("failed to deploy " + (webzServer.rootWebzApp == null ? "root WebZ App" : webzServer.rootWebzApp), e);
			}
		}

		LOG.info(webzServer + " started\n");
		return webzServer;
	}

	@Override
	public void destroy() {

		rootWebzApp = null;
		LOG.info(toString() + " stopped\n");

		globalFactory.destroy();
		LOG.info(toString() + " destroyed\n");
	}

	private void traceRequestStart(HttpServletRequest req) {
		if (LOG.isTraceEnabled()) {
			String modifiedSince = req.getHeader(WebzFilter.HEADER_IF_MODIFIED_SINCE);
			LOG.trace("\n\n\n\n// ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\\n "
					+ WebzUtils.formatRequestMethodAndUrl(req)
					+ "\n\\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ //"
					+ (modifiedSince == null ? "" : "\n " + WebzFilter.HEADER_IF_MODIFIED_SINCE + ": " + modifiedSince
							+ "\n// ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\") + "\n");
		}
	}

	private void traceRequestEnd(HttpServletRequest req, HttpServletResponse resp) {
		if (LOG.isTraceEnabled()) {
			String lastModified = resp.getHeader(WebzFilter.HEADER_LAST_MODIFIED);
			String contentLength = resp.getHeader(WebzFilter.HEADER_CONTENT_LENGTH);
			String contentType = resp.getHeader(WebzFilter.HEADER_CONTENT_TYPE);
			LOG.trace("\n\n\\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ //\n HTTP "
					+ resp.getStatus()
					+ " ("
					+ WebzUtils.formatRequestMethodAndUrl(req)
					+ ")"
					+ (contentLength == null ? "" : " - " + contentLength + " bytes")
					+ "\n// ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\"
					+ (lastModified == null ? "" : "\n " + WebzFilter.HEADER_LAST_MODIFIED + ": " + lastModified)
					+ (contentType == null ? "" : "\n " + WebzFilter.HEADER_CONTENT_TYPE + ": " + contentType)
					+ (lastModified == null && contentType == null ? ""
							: "\n\\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ //")
					+ "\n\n\n");
		}
	}

	@Override
	public String toString() {
		return "WebZ Server ( " + super.toString() + " )";
	}

}
