package org.terems.webz.base;

import java.io.IOException;

import org.terems.webz.WebzConfig;
import org.terems.webz.WebzException;
import org.terems.webz.plugin.WebzConfigObject;

/** TODO !!! describe !!! **/
public abstract class WebzConfigProxy implements WebzConfig {

	/** TODO !!! describe !!! **/
	protected abstract WebzConfig getInnerConfig() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	@Override
	public <T extends WebzConfigObject> T getConfigObject(Class<T> configObjectClass) throws IOException, WebzException {
		return getInnerConfig().getConfigObject(configObjectClass);
	}

}
