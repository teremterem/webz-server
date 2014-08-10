package org.terems.webz.plugin;

import java.io.IOException;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFilterConfig;

/** TODO !!! describe !!! **/
public abstract class BaseWebzFilter implements WebzFilter {

	private WebzFilterConfig filterConfig;

	/** TODO !!! describe !!! **/
	public WebzFilterConfig getFilterConfig() {
		return filterConfig;
	}

	/** TODO !!! describe !!! **/
	@Override
	public void init(WebzFilterConfig filterConfig) throws IOException, WebzException {
		this.filterConfig = filterConfig;
	}

	/** TODO !!! describe !!! **/
	@Override
	public void destroy() {
	}

}
