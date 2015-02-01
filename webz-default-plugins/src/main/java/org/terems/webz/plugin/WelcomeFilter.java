package org.terems.webz.plugin;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzContext;
import org.terems.webz.WebzDefaults;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzMetadata;
import org.terems.webz.base.BaseWebzFilter;
import org.terems.webz.base.WebzContextProxy;
import org.terems.webz.config.GeneralAppConfig;
import org.terems.webz.util.WebzUtils;

public class WelcomeFilter extends BaseWebzFilter {

	private Collection<String> welcomeExtensionsLowerCased;
	private Collection<String> welcomeFilenamesLowerCased;

	// dilemma: 301 (permanent) redirect is more SEO friendly but some browsers and crawlers may treat it as "eternal"
	private final boolean permanentRedirect = false;

	@Override
	public void init() throws WebzException {
		GeneralAppConfig generalConfig = getAppConfig().getAppConfigObject(GeneralAppConfig.class);
		welcomeExtensionsLowerCased = generalConfig.getWelcomeExtensionsLowerCased();
		welcomeFilenamesLowerCased = generalConfig.getWelcomeFilenamesLowerCased();
	}

	@Override
	public void serve(HttpServletRequest req, HttpServletResponse resp, final WebzChainContext chainContext) throws IOException,
			WebzException {

		redirectFileFolder(req, resp, chainContext);
	}

	public void redirectFileFolder(HttpServletRequest req, HttpServletResponse resp, final WebzChainContext nonProxiedChainContext)
			throws IOException, WebzException {

		boolean isMethodHead = WebzUtils.isHttpMethodHead(req);

		if (isMethodHead || WebzUtils.isHttpMethodGet(req)) {

			// resolving file using default resolver...
			WebzMetadata metadata = nonProxiedChainContext.resolveFile(req).getMetadata();
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
		nonProxiedChainContext.nextPlease(req, resp, new WelcomeContextProxy(nonProxiedChainContext));
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
		resp.setHeader(HEADER_LOCATION, redirectUrl);

		if (!isMethodHead) {
			resp.getWriter().write("Redirect to " + redirectUrl);
		}
	}

	private class WelcomeContextProxy extends WebzContextProxy {

		private WebzChainContext chainContext;

		private WelcomeContextProxy(WebzChainContext chainContext) {
			this.chainContext = chainContext;
		}

		@Override
		public WebzFile resolveFile(HttpServletRequest req) throws IOException, WebzException {

			WebzFile file = super.resolveFile(req);

			WebzMetadata metadata = file.getMetadata();
			if (metadata != null && !file.isPathnameInvalid() && !metadata.isFile()) {

				String parentFolderNameLowerCased = null;

				Map<String, WebzFile> childrenMap = buildChildrenMap(file);

				for (String welcomeExtensionLowerCased : welcomeExtensionsLowerCased) {

					for (String welcomeFilenameLowerCased : welcomeFilenamesLowerCased) {

						if (WebzDefaults.USE_PARENT_FOLDER_NAME.equals(welcomeFilenameLowerCased)) {
							if (parentFolderNameLowerCased == null) {
								parentFolderNameLowerCased = WebzUtils.toLowerCaseEng(metadata.getName());
							}
							welcomeFilenameLowerCased = parentFolderNameLowerCased;
						}
						WebzFile child = childrenMap.get(welcomeFilenameLowerCased + welcomeExtensionLowerCased);

						if (child != null) {
							return child;
						}
					}
				}
			}

			return file;
		}

		private Map<String, WebzFile> buildChildrenMap(WebzFile file) throws IOException, WebzException {
			Map<String, WebzFile> childrenMap = new HashMap<String, WebzFile>();

			for (WebzFile child : file.listChildren()) {
				String childPathname = child.getPathname();

				if (childPathname != null) {
					childrenMap.put(WebzUtils.toLowerCaseEng(childPathname.substring(childPathname.lastIndexOf('/') + 1)), child);
				}
			}

			return childrenMap;
		}

		@Override
		protected WebzContext getInnerContext() {
			return chainContext;
		}
	}

}
