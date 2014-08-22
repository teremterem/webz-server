package org.terems.webz;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

/** TODO !!! describe !!! **/
public abstract class WebzContextProxy extends WebzConfigProxy implements WebzContext {

	/** TODO !!! describe !!! **/
	protected abstract WebzContext getInnerContext() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	@Override
	protected WebzConfig getInnerConfig() throws IOException, WebzException {
		return getInnerContext();
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzFile resolveFile(HttpServletRequest req) throws IOException, WebzException {
		return getInnerContext().resolveFile(req);
	}

}
