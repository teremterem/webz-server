package org.terems.webz.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzMetadata;
import org.terems.webz.base.WebzContextProxy;
import org.terems.webz.plugin.base.BaseWebzFilter;

public class WelcomeFilter extends BaseWebzFilter {

	// TODO move it to config folder
	private Collection<String> defaultFileExtensions = Arrays.asList(new String[] { ".html" });
	private Collection<String> defaultFileNames = Arrays.asList(new String[] { "index" });

	@Override
	public void service(HttpServletRequest req, HttpServletResponse resp, final WebzChainContext chainContext) throws IOException,
			WebzException {

		// TODO

		redirectFileFolder(req, resp, chainContext);
	}

	public void redirectFileFolder(HttpServletRequest req, HttpServletResponse resp, final WebzChainContext chainContext)
			throws IOException, WebzException {

		String requestMethod = req.getMethod();
		boolean isMethodHead = "HEAD".equals(requestMethod);

		if (("GET".equals(requestMethod) || isMethodHead)) {

			// resolving file using default resolver...
			WebzMetadata metadata = chainContext.resolveFile(req).getMetadata();
			if (metadata != null) {

				boolean uriEndsWithSlash = req.getRequestURI().endsWith("/");

				if (!uriEndsWithSlash && metadata.isFolder()) {

					doRedirect(req, resp, true, isMethodHead);
					return;

				} else if (uriEndsWithSlash && metadata.isFile()) {

					doRedirect(req, resp, false, isMethodHead);
					return;

				}
			}
		}
		chainContext.nextPlease(req, resp, new WebzContextProxy() {

			@Override
			public WebzFile resolveFile(HttpServletRequest req) throws IOException, WebzException {

				// TODO

				return super.resolveFile(req);
			}

			@Override
			protected WebzContext getInnerContext() {
				return chainContext;
			}
		});
	}

	private void doRedirect(HttpServletRequest req, HttpServletResponse resp, boolean toFolder, boolean isMethodHead) throws IOException {

		StringBuffer urlBuffer = req.getRequestURL();
		if (toFolder) {
			urlBuffer.append('/');
		} else {
			urlBuffer.setLength(urlBuffer.length() - 1);
		}

		String queryString = req.getQueryString();
		if (queryString != null) {
			urlBuffer.append('?').append(queryString);
		}

		String redirectUrl = urlBuffer.toString();

		// TODO dilemma: 301 (permanent) redirect is more SEO friendly but some browsers and crawlers may treat it as "eternal"
		resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
		resp.setHeader("Location", redirectUrl);

		if (!isMethodHead) {
			resp.getWriter().write("Redirect to " + redirectUrl);
		}
	}

}
