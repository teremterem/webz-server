package org.terems.webz.plugin.base;

import java.io.IOException;

import org.terems.webz.WebzConfig;
import org.terems.webz.WebzException;
import org.terems.webz.plugin.WebzFilter;

/** TODO !!! describe !!! **/
public abstract class BaseWebzFilter implements WebzFilter {

	private WebzConfig appConfig;

	/** TODO !!! describe !!! **/
	public WebzConfig getAppConfig() {
		return appConfig;
	}

	/** TODO !!! describe !!! **/
	@Override
	public void init(WebzConfig appConfig) throws IOException, WebzException {
		this.appConfig = appConfig;
	}

	/** Do nothing by default... **/
	@Override
	public void destroy() {
	}

}
