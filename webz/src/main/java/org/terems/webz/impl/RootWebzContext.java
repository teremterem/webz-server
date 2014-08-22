package org.terems.webz.impl;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.terems.webz.WebzContext;
import org.terems.webz.WebzDefaults;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileFactory;

// TODO TODO refactor this ???
public class RootWebzContext implements WebzContext {

	private WebzFileFactory fileFactory;

	// TODO refactor this ???
	@Deprecated
	public static String trimFileSeparators(String pathName) {
		pathName = pathName.trim();
		if (pathName.startsWith("/") || pathName.startsWith("\\")) {
			pathName = pathName.substring(1);
		}
		if (pathName.endsWith("/") || pathName.endsWith("\\")) {
			pathName = pathName.substring(0, pathName.length() - 1);
		}
		return pathName;
	}

	public RootWebzContext(WebzFileFactory fileFactory) {
		this.fileFactory = fileFactory;
	}

	@Override
	public WebzFile resolveFile(HttpServletRequest req) {
		return getFile(req.getPathInfo());
	}

	@Override
	public WebzFile resolveConfigFolder() throws IOException, WebzException {
		return getFile(WebzDefaults.WEBZ_CONFIG_FOLDER);
	}

	@Override
	public WebzFile getFile(String pathInfo) {

		// TODO revise path normalization logic
		// TODO + force 404(?) for cases like http://localhost:8080//////webz-pedesis.html
		String pathName = pathInfo == null ? "" : trimFileSeparators(pathInfo);

		return fileFactory.get(pathName);
	}

}
