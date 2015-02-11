/*
 * WebZ Server is a server that can serve web pages from various sources.
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

package org.terems.webz.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFilter;
import org.terems.webz.filter.ErrorFilter;
import org.terems.webz.filter.NotFoundFilter;
import org.terems.webz.filter.StaticContentFilter;
import org.terems.webz.filter.WelcomeFilter;
import org.terems.webz.impl.WebzServer;
import org.terems.webz.util.WebzUtils;

@SuppressWarnings("serial")
public class WebzHttpServletEnvelope extends HttpServlet {

	private static final String ROOT_FILE_SYSTEM_PROPERTIES_PARAM = "rootFileSystemProperties";

	@Override
	public void init() throws ServletException {
		try {
			// TODO decide if WebzServer lazy initialization mechanism is needed at all
			webzServer();

		} catch (IOException e) {
			throw new ServletException(e);
		} catch (WebzException e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		try {
			webzServer().serve(req, resp);

		} catch (WebzException e) {
			throw new ServletException(e);
		}
	}

	/**
	 * <a href="http://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java">Double-checked locking - Usage in Java</a>
	 **/
	private WebzServer webzServer() throws IOException, WebzException {

		WebzServer webzServer = this.webzServer;
		if (webzServer == null) {

			synchronized (webzServerMutex) {

				webzServer = this.webzServer;
				if (webzServer == null) {

					Properties rootFileSystemProperties = WebzUtils.loadPropertiesFromClasspath(
							getServletConfig().getInitParameter(ROOT_FILE_SYSTEM_PROPERTIES_PARAM), true);

					// // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ //
					this.webzServer = webzServer = new WebzServer(rootFileSystemProperties, getDefaultFilterClassesList());
					// \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\
				}
			}
		}
		return webzServer;
	}

	private volatile WebzServer webzServer;
	private final Object webzServerMutex = new Object();

	@Override
	public void destroy() {

		synchronized (webzServerMutex) {

			WebzServer webzServer = this.webzServer;
			if (webzServer != null) {

				this.webzServer = null;
				webzServer.destroy();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Collection<Class<? extends WebzFilter>> getDefaultFilterClassesList() {

		// TODO move list of filters into webz app config
		Class<?>[] filterClassesList = { ErrorFilter.class, WelcomeFilter.class, StaticContentFilter.class, NotFoundFilter.class };

		return Arrays.asList((Class<? extends WebzFilter>[]) filterClassesList);
	}

}
