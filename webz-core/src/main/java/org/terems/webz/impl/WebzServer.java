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
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFilter;
import org.terems.webz.WebzProperties;
import org.terems.webz.filter.ErrorFilter;
import org.terems.webz.filter.ForcedRedirectsFilter;
import org.terems.webz.filter.MarkdownForSpaFilter;
import org.terems.webz.filter.NotFoundFilter;
import org.terems.webz.filter.StaticContentFilter;
import org.terems.webz.filter.WelcomeFilter;
import org.terems.webz.internals.WebzApp;
import org.terems.webz.internals.WebzDestroyableObjectFactory;
import org.terems.webz.internals.WebzFileSystem;
import org.terems.webz.internals.WebzServletContainerBridge;
import org.terems.webz.util.WebzUtils;

public class WebzServer implements WebzServletContainerBridge {

	private static final Logger LOG = LoggerFactory.getLogger(WebzServer.class);

	private static final String WEBZ_INTERNAL_PROPERTIES_RESOURCE = "webz-internal.properties";

	@SuppressWarnings("unchecked")
	private static final Collection<Class<? extends WebzFilter>> DEFAULT_FILTERS = Arrays
			.asList((Class<? extends WebzFilter>[]) new Class<?>[] { ErrorFilter.class, WelcomeFilter.class, ForcedRedirectsFilter.class,
					MarkdownForSpaFilter.class, StaticContentFilter.class, NotFoundFilter.class });

	private WebzDestroyableObjectFactory globalFactory = new GenericWebzObjectFactory();
	private volatile WebzApp rootWebzApp;

	private WebzProperties webzInternalProperties;

	public WebzServer() {

		try {
			Properties internalProperties = new Properties();
			WebzUtils.loadPropertiesFromClasspath(internalProperties, WEBZ_INTERNAL_PROPERTIES_RESOURCE, true);

			webzInternalProperties = new WebzProperties(internalProperties);

		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (WebzException e) {
			throw new RuntimeException(e);
		}
	}

	public void start(String siteContentPath, String renderingSpaPath) {

		WebzProperties siteFileSystemProperties = new WebzProperties(webzInternalProperties);
		siteFileSystemProperties.put(WebzProperties.FS_BASE_PATH_PROPERTY, siteContentPath);

		WebzProperties spaFileSystemProperties = new WebzProperties(webzInternalProperties);
		spaFileSystemProperties.put(WebzProperties.FS_BASE_PATH_PROPERTY, renderingSpaPath);

		try {
			WebzFileSystemManager fileSystemManager = WebzFileSystemManager.getManager(globalFactory);

			WebzFileSystem siteFileSystem = fileSystemManager.createFileSystem(siteFileSystemProperties);
			WebzFileSystem spaFileSystem = fileSystemManager.createFileSystem(spaFileSystemProperties);

			WebzFileSystem siteAndSpaFileSystem = fileSystemManager.createSiteAndSpaFileSystem(siteFileSystem, spaFileSystem,
					webzInternalProperties);

			rootWebzApp = globalFactory.newDestroyable(GenericWebzApp.class);
			rootWebzApp.init(siteAndSpaFileSystem, DEFAULT_FILTERS, globalFactory.newDestroyable(GenericWebzObjectFactory.class));

		} catch (WebzException e) {

			if (LOG.isErrorEnabled()) {
				LOG.error("failed to init WebZ App" + (rootWebzApp == null ? "" : " \"" + rootWebzApp.getDisplayName() + "\""), e);
			}
		}

		LOG.info("WebZ Server started\n");
	}

	@Override
	public void serve(HttpServletRequest req, HttpServletResponse resp) throws IOException, WebzException {

		if (rootWebzApp == null) {
			throw new WebzException("root WebZ App is already stopped or has not been started yet");
		}

		traceRequestStart(req);

		rootWebzApp.serve(req, resp);

		traceRequestEnd(req, resp);
	}

	@Override
	public void destroy() {

		rootWebzApp = null;
		LOG.info("WebZ Server stopped\n");

		globalFactory.destroy();
		LOG.info("WebZ Server destroyed\n");
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

}
