package org.terems.webz.filter;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFileMetadata;
import org.terems.webz.plugin.BaseWebzFilter;

public class FolderRedirectFilter extends BaseWebzFilter {

	@Override
	public void service(HttpServletRequest req, HttpServletResponse resp, WebzChainContext chainContext) throws IOException,
			WebzException {

		String requestMethod = req.getMethod();
		boolean methodHead = "HEAD".equals(requestMethod);

		if (("GET".equals(requestMethod) || methodHead) && !req.getRequestURI().endsWith("/")) {

			String pathInfo = req.getPathInfo();
			WebzFileMetadata metadata = chainContext.fileFactory().get(pathInfo).getMetadata();

			if (metadata != null && metadata.isFolder()) {

				String redirectUrl = constructRedirectUrl(req);

				resp.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
				resp.setHeader("Location", redirectUrl);

				if (!methodHead) {
					resp.getWriter().write("Permanently moved to <a href=\"" + redirectUrl + "\">" + redirectUrl + "</a>");
				}

				return;
			}
		}
		chainContext.nextPlease(req, resp);
	}

	private String constructRedirectUrl(HttpServletRequest req) {

		StringBuffer urlBuffer = req.getRequestURL();
		urlBuffer.append('/');

		String queryString = req.getQueryString();
		if (queryString != null) {
			urlBuffer.append('?').append(queryString);
		}

		return urlBuffer.toString();
	}

}
