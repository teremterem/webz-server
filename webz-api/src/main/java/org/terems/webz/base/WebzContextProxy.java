package org.terems.webz.base;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.terems.webz.WebzContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;

/** TODO !!! describe !!! **/
public abstract class WebzContextProxy implements WebzContext {

	/** TODO !!! describe !!! **/
	protected abstract WebzContext getInnerContext() throws IOException, WebzException;

	@Override
	public WebzFile resolveFile(HttpServletRequest req) throws IOException, WebzException {
		return getInnerContext().resolveFile(req);
	}

	@Override
	public WebzFile getFile(String pathInfo) throws IOException, WebzException {
		return getInnerContext().getFile(pathInfo);
	}

}
