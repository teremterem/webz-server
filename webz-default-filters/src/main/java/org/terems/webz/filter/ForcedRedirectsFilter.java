/*
 * WebZ Server can serve web pages from various local and remote file sources.
 * Copyright (C) 2014-2015  Oleksandr Tereschenko <http://www.terems.org/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.terems.webz.filter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzMetadata;
import org.terems.webz.base.BaseWebzFilter;
import org.terems.webz.config.ForcedRedirectsConfig;
import org.terems.webz.util.WebzUtils;

public class ForcedRedirectsFilter extends BaseWebzFilter {

	public static final String URL_PATTERN_PATHNAME_PARAM = "{pathname}";

	private String forcedRedirectsQueryParam;
	private boolean forcedRedirectsPermanent;

	private ForcedRedirectsConfig redirectsConfig;

	@Override
	public void init() throws WebzException {

		redirectsConfig = getAppConfig().getAppConfigObject(ForcedRedirectsConfig.class);

		forcedRedirectsQueryParam = redirectsConfig.getForcedRedirectsQueryParam();
		forcedRedirectsPermanent = redirectsConfig.isForcedRedirectsPermanent();
	}

	@Override
	public void serve(HttpServletRequest req, HttpServletResponse resp, WebzChainContext chainContext) throws IOException, WebzException {

		if (!serveRedirect(req, resp, chainContext)) {
			chainContext.nextPlease(req, resp);
		}
	}

	private boolean serveRedirect(HttpServletRequest req, HttpServletResponse resp, WebzContext context) throws IOException, WebzException {

		if (forcedRedirectsQueryParam == null) {
			return false;
		}
		String queryParamValue = req.getParameter(forcedRedirectsQueryParam);
		if (queryParamValue == null) {
			return false;
		}
		String urlPattern = redirectsConfig.getForcedRedirectsUrlPattern(queryParamValue);
		if (urlPattern == null) {
			return false;
		}

		WebzFile file = context.resolveFile(req);
		WebzMetadata metadata = file.getMetadata();
		if (metadata == null) {
			return false;
		}

		String pathname = metadata.getLinkedPathname();
		if (pathname == null) {
			pathname = file.getPathname();
		}

		URL url = new URL(urlPattern);
		try {
			URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), injectPathname(url.getPath(), pathname),
					injectPathname(url.getQuery(), pathname), injectPathname(url.getRef(), pathname));

			WebzUtils.doRedirect(resp, uri.toASCIIString(), forcedRedirectsPermanent, WebzUtils.isHttpMethodHead(req));

		} catch (URISyntaxException e) {
			throw new WebzException(e);
		}
		return true;
	}

	private String injectPathname(String template, String pathname) {

		if (template == null) {
			return null;
		}
		return template.replace(URL_PATTERN_PATHNAME_PARAM, pathname);
	}

}
