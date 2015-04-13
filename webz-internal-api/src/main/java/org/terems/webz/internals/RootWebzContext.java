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

package org.terems.webz.internals;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzConfig;
import org.terems.webz.WebzContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzMetadata;
import org.terems.webz.WebzPathnameException;
import org.terems.webz.WebzProperties;
import org.terems.webz.config.WebzConfigObject;

public class RootWebzContext implements WebzContext, WebzConfig {

	private static final Logger LOG = LoggerFactory.getLogger(RootWebzContext.class);

	private WebzFileFactory fileFactory;
	private WebzObjectFactory appFactory;

	public RootWebzContext(WebzFileFactory fileFactory, WebzObjectFactory appFactory) {
		this.fileFactory = fileFactory;
		this.appFactory = appFactory;
	}

	@Override
	public WebzFile resolveFile(HttpServletRequest req) {
		return getFile(req.getPathInfo());
	}

	@Override
	public WebzFile getFile(String pathInfo) {

		WebzFile file = fileFactory.get(pathInfo == null ? "" : pathInfo);
		try {
			if (file.isHidden()) {
				return new WebzFileNotAccessible(file);
			}

		} catch (WebzPathnameException e) {
			return new WebzFileNotAccessible(file);
		}

		return file;
	}

	@Override
	public String resolveUri(WebzFile file, HttpServletRequest req) throws IOException, WebzException {

		if (file == null) {
			return null;
		}
		String pathname = file.getPathname();
		if (pathname == null) {
			return null;
		}
		WebzMetadata metadata = file.getMetadata();
		if (metadata != null) {

			String linkedPathname = metadata.getLinkedPathname();
			if (linkedPathname != null) {
				pathname = linkedPathname;
			}
			if (metadata.isFolder() && pathname.length() > 0) {
				pathname += '/';
			}
		}
		try {
			return new URI(null, req.getContextPath() + '/' + pathname, null).toASCIIString();
			// TODO take WebZ App context path into account as well if/when it is introduced

		} catch (URISyntaxException e) {
			throw new WebzException(e);
		}
	}

	@Override
	public <T extends WebzConfigObject> T getConfigObject(Class<T> configObjectClass) throws WebzException {

		T configObject = appFactory.getDestroyableSingleton(configObjectClass);

		if (configObject.doOneTimeInit(getConfigFolder()) && LOG.isInfoEnabled()) {
			LOG.info("config object '" + configObject.getClass().getName() + "' initialized");
		}
		return configObject;
	}

	private WebzFile getConfigFolder() {

		WebzFile configFolder = fileFactory.get(WebzProperties.WEBZ_CONFIG_FOLDER);
		if (configFolder.isPathnameInvalid()) {
			// should not happen
			throw new RuntimeException("WebZ doesn't recognize '" + configFolder.getPathname() + "' as a valid pathname");
		}

		return configFolder;
	}

}
