package org.terems.webz;

import java.io.IOException;

/** TODO !!! describe !!! **/
public abstract class WebzConfigProxy implements WebzConfig {

	/** TODO !!! describe !!! **/
	protected abstract WebzConfig getInnerConfig() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	@Override
	public WebzFile resolveConfigFolder() throws IOException, WebzException {
		return getInnerConfig().resolveConfigFolder();
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzFile getFile(String pathInfo) throws IOException, WebzException {
		return getInnerConfig().getFile(pathInfo);
	}

}
