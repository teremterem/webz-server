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

package org.terems.webz.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.WebzLaunchHelper;
import org.terems.webz.WebzException;
import org.terems.webz.WebzProperties;
import org.terems.webz.impl.AbstractWebzHttpServlet;
import org.terems.webz.impl.WebzServer;
import org.terems.webz.util.WebzUtils;

@SuppressWarnings("serial")
public class WebzServerHttpServlet extends AbstractWebzHttpServlet {

	private static final Logger LOG = LoggerFactory.getLogger(WebzServerHttpServlet.class);

	private static final String WEBZ_SERVER_INTERNAL_PROPERTIES_RESOURCE = "webz-server-internal.properties";

	@Override
	protected WebzServer initWebzServer() throws IOException, WebzException {

		Properties internalProperties = new Properties();
		WebzUtils.loadPropertiesFromClasspath(internalProperties, WEBZ_SERVER_INTERNAL_PROPERTIES_RESOURCE, true);
		WebzProperties webzInternalProperties = new WebzProperties(internalProperties);

		Properties webzProperties = fetchWebzProperties();

		String siteContentPath = webzProperties.getProperty(WebzLaunchHelper.SITE_CONTENT_PATH_PROPERTY);
		String renderingSpaPath = webzProperties.getProperty(WebzLaunchHelper.RENDERING_SPA_PATH_PROPERTY);
		String webzBoilerplatePath = webzProperties.getProperty(WebzLaunchHelper.WEBZ_BOILERPLATE_PATH_PROPERTY);

		if (renderingSpaPath == null) {
			throw new WebzException(WebzLaunchHelper.RENDERING_SPA_PATH_PROPERTY + " WebZ property is not set");
		}
		if (siteContentPath == null) {
			throw new WebzException(WebzLaunchHelper.SITE_CONTENT_PATH_PROPERTY + " WebZ property is not set");
		}

		WebzProperties siteProperties = new WebzProperties(webzInternalProperties);
		siteProperties.put(WebzProperties.FS_BASE_PATH_PROPERTY, siteContentPath);

		WebzProperties spaProperties = new WebzProperties(webzInternalProperties);
		spaProperties.put(WebzProperties.FS_BASE_PATH_PROPERTY, renderingSpaPath);

		WebzProperties boilerplateProperties = null;
		if (webzBoilerplatePath != null) {
			boilerplateProperties = new WebzProperties(webzInternalProperties);
			boilerplateProperties.put(WebzProperties.FS_BASE_PATH_PROPERTY, webzBoilerplatePath);
		}

		// // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ //
		return WebzServer.start(siteProperties, spaProperties, boilerplateProperties, webzInternalProperties);
		// \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\
	}

	private Properties fetchWebzProperties() throws IOException {

		Properties webzProperties = new Properties();

		File file = null;
		String path = System.getProperty(WebzLaunchHelper.WEBZ_PROPERTIES_PROPERTY);
		if (path == null) {
			path = System.getenv(WebzLaunchHelper.WEBZ_PROPERTIES_ENV_VAR);

			if (path != null && LOG.isInfoEnabled()) {
				LOG.info(WebzLaunchHelper.getUsingEnvVarMessage(WebzLaunchHelper.WEBZ_PROPERTIES_ENV_VAR));
			}
		} else {
			file = new File(path);
		}

		if (file == null || !(file.exists() && file.isFile())) {
			if (LOG.isWarnEnabled()) {
				LOG.warn(WebzLaunchHelper.getPropertiesNotLoadedMessage(file));
			}
		} else {
			if (LOG.isInfoEnabled()) {
				LOG.info(WebzLaunchHelper.getPropertiesPathMessage(file));
			}
			webzProperties.load(new FileInputStream(file));
		}

		return webzProperties;
	}

}
