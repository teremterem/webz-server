package org.terems.webz.impl;

import javax.servlet.http.HttpServletRequest;

import org.terems.webz.WebzConfig;
import org.terems.webz.WebzContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileFactory;
import org.terems.webz.plugin.WebzConfigObject;

public class RootWebzContext implements WebzContext, WebzConfig {

	private WebzFileFactory fileFactory;

	public RootWebzContext(WebzFileFactory fileFactory) {
		this.fileFactory = fileFactory;
	}

	@Override
	public WebzFile resolveFile(HttpServletRequest req) {
		return getFile(req.getPathInfo());
	}

	@Override
	public WebzFile getFile(String pathInfo) {
		// TODO make sure WEBZ_CONFIG_FOLDER is not accessible from here
		return fileFactory.get(pathInfo == null ? "" : pathInfo);
	}

	@Override
	public <T extends WebzConfigObject> T getAppConfigObject(Class<T> configObjectClass) throws WebzException {
		// TODO T configObject = new BlaBlaConfigObject();
		// configObject.init(fileFactory.get(WebzDefaults.WEBZ_CONFIG_FOLDER));
		return null;
	}

}
