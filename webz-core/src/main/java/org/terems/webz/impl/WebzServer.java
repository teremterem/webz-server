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
import java.util.Collection;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFilter;
import org.terems.webz.internals.WebzApp;
import org.terems.webz.internals.WebzDestroyableObjectFactory;
import org.terems.webz.internals.WebzFileSystem;
import org.terems.webz.internals.WebzServletContainerBridge;
import org.terems.webz.util.WebzUtils;

public class WebzServer implements WebzServletContainerBridge {

	private static final Logger LOG = LoggerFactory.getLogger(WebzServer.class);

	private WebzDestroyableObjectFactory globalFactory = new GenericWebzObjectFactory();
	private volatile WebzApp rootWebzApp;

	public WebzServer(Properties rootFileSystemProperties, Collection<Class<? extends WebzFilter>> filterClassesList) {

		try {
			WebzFileSystem rootFileSystem = WebzFileSystemManager.getManager(globalFactory).createFileSystem(rootFileSystemProperties);

			rootWebzApp = globalFactory.newDestroyable(GenericWebzApp.class).init(rootFileSystem, filterClassesList,
					globalFactory.newDestroyable(GenericWebzObjectFactory.class));

		} catch (WebzException e) {

			if (LOG.isErrorEnabled()) {
				LOG.error("failed to init WebZ App '" + (rootWebzApp == null ? null : rootWebzApp.getDisplayName()) + "'", e);
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
