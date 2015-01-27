package org.terems.webz.impl;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzConfig;
import org.terems.webz.WebzContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzPathnameException;
import org.terems.webz.WebzProperties;
import org.terems.webz.config.WebzConfigObject;
import org.terems.webz.internals.WebzFileFactory;
import org.terems.webz.internals.WebzFileNotAccessible;
import org.terems.webz.internals.WebzObjectFactory;

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
			if (file.belongsToSubtree(getConfigFolder())) {
				return new WebzFileNotAccessible(file);
			}
			// TODO hide all files and folders starting with dot (not just .webz-config)

		} catch (WebzPathnameException e) {
			return new WebzFileNotAccessible(file, e);
		}

		return file;
	}

	@Override
	public <T extends WebzConfigObject> T getAppConfigObject(Class<T> configObjectClass) throws WebzException {

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
			throw new RuntimeException("file system doesn't recognize '" + configFolder.getPathname() + "' as a valid pathname");
		}

		return configFolder;
	}

}
