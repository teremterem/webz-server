package org.terems.webz.plugin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.terems.webz.WebzException;
import org.terems.webz.WebzConfig;

/** TODO !!! describe !!! **/
public abstract class BaseWebzFilter implements WebzFilter {

	private WebzConfig filterConfig;

	/** TODO !!! describe !!! **/
	public WebzConfig getFilterConfig() {
		return filterConfig;
	}

	/** TODO !!! describe !!! **/
	@Override
	public void init(WebzConfig filterConfig) throws IOException, WebzException {
		this.filterConfig = filterConfig;
	}

	// TODO TODO TODO TODO TODO TODO TODO TODO
	// TODO TODO TODO TODO TODO TODO TODO TODO
	// TODO TODO TODO TODO TODO TODO TODO TODO
	// TODO TODO TODO TODO TODO TODO TODO TODO
	// TODO TODO TODO TODO TODO TODO TODO TODO
	// TODO support last modified concept TODO
	// TODO TODO TODO TODO TODO TODO TODO TODO
	// TODO TODO TODO TODO TODO TODO TODO TODO
	// TODO TODO TODO TODO TODO TODO TODO TODO
	// TODO TODO TODO TODO TODO TODO TODO TODO
	// TODO TODO TODO TODO TODO TODO TODO TODO

	/** TODO !!! describe !!! **/
	protected long getLastModified(HttpServletRequest req) {
		return -1;
		// TODO TODO TODO TODO TODO
		// TODO TODO TODO TODO TODO
		// TODO TODO TODO TODO TODO
		// TODO TODO TODO TODO TODO
		// TODO TODO TODO TODO TODO
	}

	/** TODO !!! describe !!! **/
	@Override
	public void destroy() {
	}

}
