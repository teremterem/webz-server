package org.terems.webz.plugin;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzMetadata;
import org.terems.webz.base.BaseWebzFilter;
import org.terems.webz.base.WebzContextProxy;
import org.terems.webz.config.GeneralAppConfig;
import org.terems.webz.util.WebzUtils;

public class WelcomeFilter extends BaseWebzFilter {

	private String[] welcomeExtensions;
	private String[] welcomeFilenames;

	// dilemma: 301 (permanent) redirect is more SEO friendly but some browsers and crawlers may treat it as "eternal"
	private final boolean permanentRedirect = false;

	@Override
	public void init() throws WebzException {
		GeneralAppConfig generalConfig = getAppConfig().getAppConfigObject(GeneralAppConfig.class);
		welcomeExtensions = WebzUtils.parseCsvLine(generalConfig.getWelcomeExtensionsList());
		welcomeFilenames = WebzUtils.parseCsvLine(generalConfig.getWelcomeFilenamesList());
	}

	@Override
	public void serve(HttpServletRequest req, HttpServletResponse resp, final WebzChainContext chainContext) throws IOException,
			WebzException {

		redirectFileFolder(req, resp, chainContext);
	}

	public void redirectFileFolder(HttpServletRequest req, HttpServletResponse resp, final WebzChainContext chainContext)
			throws IOException, WebzException {

		String requestMethod = req.getMethod();
		boolean isMethodHead = HTTP_HEAD.equals(requestMethod);

		if ((HTTP_GET.equals(requestMethod) || isMethodHead)) {

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
		chainContext.nextPlease(req, resp, new WelcomeContextProxy(chainContext));
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

		resp.setStatus(permanentRedirect ? HttpServletResponse.SC_MOVED_PERMANENTLY : HttpServletResponse.SC_MOVED_TEMPORARILY);
		resp.setHeader("Location", redirectUrl);

		if (!isMethodHead) {
			resp.getWriter().write("Redirect to " + redirectUrl);
		}
	}

	private class WelcomeContextProxy extends WebzContextProxy {

		private WebzChainContext chainContext;

		private WelcomeContextProxy(WebzChainContext chainContext) {
			this.chainContext = chainContext;
		}

		private boolean checkFilename(WebzFile file, String extension, String filename) {

			String filePathname = file.getPathname().toLowerCase(Locale.ENGLISH);
			String expectedFilename = filename.toLowerCase(Locale.ENGLISH) + extension.toLowerCase(Locale.ENGLISH);

			if (filePathname.length() < expectedFilename.length()) {
				return false;

			} else if (filePathname.length() == expectedFilename.length()) {

				return filePathname.equals(expectedFilename);
			} else {
				return filePathname.endsWith("/" + expectedFilename);
			}
		}

		@Override
		public WebzFile resolveFile(HttpServletRequest req) throws IOException, WebzException {

			WebzFile file = super.resolveFile(req);

			WebzMetadata metadata = file.getMetadata();
			if (metadata != null && !file.isPathnameInvalid() && !metadata.isFile()) {

				for (WebzFile child : file.listChildren()) {
					for (String extension : welcomeExtensions) {

						for (String filename : welcomeFilenames) {
							if (checkFilename(child, extension, filename)) {
								return child;
							}
						}
						if (checkFilename(child, extension, metadata.getName())) {
							return child;
						}
					}
				}
			}

			return file;
		}

		@Override
		protected WebzContext getInnerContext() {
			return chainContext;
		}
	}

}
