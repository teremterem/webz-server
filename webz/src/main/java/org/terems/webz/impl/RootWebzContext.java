package org.terems.webz.impl;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.terems.webz.WebzPathnameException;
import org.terems.webz.WebzConfig;
import org.terems.webz.WebzContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileFactory;
import org.terems.webz.WebzFileNotAccessible;
import org.terems.webz.plugin.WebzConfigObject;
import org.terems.webz.settings.WebzProperties;

public class RootWebzContext implements WebzContext, WebzConfig {

	private WebzFileFactory fileFactory;
	private WebzDestroyableFactory appFactory;

	public RootWebzContext(WebzFileFactory fileFactory, WebzDestroyableFactory appFactory) {
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

		} catch (WebzPathnameException e) {
			return new WebzFileNotAccessible(file, e);
		}

		return file;
	}

	@Override
	public <T extends WebzConfigObject> T getAppConfigObject(Class<T> configObjectClass) throws WebzException {

		T configObject = appFactory.getDestroyableSingleton(configObjectClass);
		try {
			configObject.init(getConfigFolder());
		} catch (IOException e) {
			throw new WebzException(e);
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
