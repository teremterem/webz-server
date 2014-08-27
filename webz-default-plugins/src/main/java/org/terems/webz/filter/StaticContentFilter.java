package org.terems.webz.filter;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzMetadata;
import org.terems.webz.plugin.base.BaseWebzFilter;

public class StaticContentFilter extends BaseWebzFilter {

	@Override
	public void service(HttpServletRequest req, HttpServletResponse resp, final WebzChainContext chainContext) throws IOException,
			WebzException {

		WebzFile file = chainContext.resolveFile(req);

		WebzMetadata metadata = file.fileContentToOutputStream(resp.getOutputStream());

		if (metadata == null || !metadata.isFile()) {
			// file does not exist or is not a file - invoke the next filter in the chain (filter chain usually ends with NotFoundFilter)
			chainContext.nextPlease(req, resp);
		}
	}

}
