package org.terems.webz;

import javax.servlet.http.HttpServletRequest;

/** TODO !!! describe !!! **/
public abstract class WebzContextProxy implements WebzContext {

	protected abstract WebzContext getInnerContext();

	@Override
	public WebzFile resolveFileFromRequest(HttpServletRequest req) {
		return getInnerContext().resolveFileFromRequest(req);
	}

	@Override
	public WebzFile resolveFile(String pathInfo) {
		return getInnerContext().resolveFile(pathInfo);
	}

}
