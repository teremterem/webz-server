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

	/** Do nothing by default... **/
	public void init() throws WebzException {
	}

	/** TODO !!! describe !!! **/
	@Override
	public final void init(WebzConfig appConfig) throws WebzException {
		this.appConfig = appConfig;
		init();
	}

	/** Do nothing by default... **/
	@Override
	public void destroy() {
	}

}
