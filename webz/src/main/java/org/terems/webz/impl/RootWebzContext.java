package org.terems.webz.impl;

import javax.servlet.http.HttpServletRequest;

import org.terems.webz.WebzContext;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileFactory;

/** TODO !!! describe !!! **/
public class RootWebzContext implements WebzContext {

	private WebzFileFactory fileFactory;

	// TODO refactor this ?
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

	/** TODO !!! describe !!! **/
	@Override
	public WebzFile resolveFileFromRequest(HttpServletRequest req) {
		return resolveFile(req.getPathInfo());
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzFile resolveFile(String pathInfo) {

		// TODO revise path normalization logic
		// TODO + force 404(?) for cases like http://localhost:8080//////webz-pedesis.html
		String pathName = pathInfo == null ? "" : trimFileSeparators(pathInfo);

		return fileFactory.get(pathName);
	}

}
