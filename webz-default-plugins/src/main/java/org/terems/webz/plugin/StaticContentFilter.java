package org.terems.webz.plugin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzMetadata;
import org.terems.webz.plugin.base.BaseLastModifiedWebzFilter;

public class StaticContentFilter extends BaseLastModifiedWebzFilter {

	private StaticContentSender contentSender;

	@Override
	public void init() throws WebzException {
		contentSender = new StaticContentSender(getAppConfig());
	}

	@Override
	public void serve(HttpServletRequest req, HttpServletResponse resp, final WebzChainContext chainContext) throws IOException,
			WebzException {

		WebzMetadata.FileSpecific fileSpecific = contentSender.serveStaticContent(resp, chainContext.resolveFile(req));

		if (fileSpecific == null) {
			// file does not exist or is not a file - invoke the next filter in the chain (filter chain usually ends with NotFoundFilter)
			chainContext.nextPlease(req, resp);
		}
	}

}
