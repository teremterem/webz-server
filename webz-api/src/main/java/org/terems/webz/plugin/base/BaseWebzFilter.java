package org.terems.webz.plugin.base;

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
	public void init() throws WebzException {
	}

	/** TODO !!! describe !!! **/
	@Override
	public void init(WebzConfig appConfig) throws WebzException {
		this.appConfig = appConfig;
		init();
	}

	/** TODO !!! describe !!! **/
	@Override
	public void destroy() {
	}

}
