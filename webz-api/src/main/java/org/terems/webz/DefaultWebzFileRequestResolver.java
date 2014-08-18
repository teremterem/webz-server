package org.terems.webz;

import javax.servlet.http.HttpServletRequest;

public class DefaultWebzFileRequestResolver implements WebzFileRequestResolver {

	@Override
	public WebzFile resolve(WebzFileFactory fileFactory, HttpServletRequest req) {
		return fileFactory.get(req.getPathInfo());
	}

}
