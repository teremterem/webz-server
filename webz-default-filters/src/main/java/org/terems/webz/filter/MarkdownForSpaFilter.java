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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pegdown.PegDownProcessor;
import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzMetadata;
import org.terems.webz.base.BaseWebzFilter;
import org.terems.webz.config.GeneralAppConfig;
import org.terems.webz.filter.helpers.FileDownloaderWithBOM;
import org.terems.webz.util.WebzUtils;

public class MarkdownForSpaFilter extends BaseWebzFilter {

	// TODO support browser caching (ETags?)

	private String markdownSuffixLowerCased;
	private String defaultEncoding;

	private ThreadLocal<PegDownProcessor> pegDownProcessor = new ThreadLocal<PegDownProcessor>() {
		@Override
		protected PegDownProcessor initialValue() {
			return new PegDownProcessor();
		}
	};

	@Override
	public void init() throws WebzException {

		GeneralAppConfig generalConfig = getAppConfig().getAppConfigObject(GeneralAppConfig.class);
		markdownSuffixLowerCased = generalConfig.getMarkdownSuffixLowerCased();
		defaultEncoding = generalConfig.getDefaultEncoding();

		String renderingTemplatePathname = generalConfig.getRenderingTemplatePathname();
	}

	@Override
	public void serve(HttpServletRequest req, HttpServletResponse resp, WebzChainContext chainContext) throws IOException, WebzException {

		if (!serveMarkdown(req, resp, chainContext)) {
			chainContext.nextPlease(req, resp);
		}
	}

	private boolean serveMarkdown(HttpServletRequest req, HttpServletResponse resp, WebzChainContext chainContext) throws IOException,
			WebzException {

		WebzFile file = chainContext.resolveFile(req);
		WebzMetadata metadata = file.getMetadata();
		if (metadata == null || !metadata.isFile() || !WebzUtils.toLowerCaseEng(metadata.getName()).endsWith(markdownSuffixLowerCased)) {
			return false;
		}

		String markdownContent = readFileAsString(file);
		if (markdownContent == null) {
			return false;
		}

		byte[] html = pegDownProcessor.get().markdownToHtml(markdownContent).getBytes(defaultEncoding);
		// TODO support configurable rel="nofollow" for links to foreign domains
		// TODO support configurable target="xxx"

		WebzUtils.prepareStandardHeaders(resp, "text/html", defaultEncoding, html.length);
		// TODO content type should probably be the same as freemarker template's mimetype

		if (!WebzUtils.isHttpMethodHead(req)) {
			resp.getOutputStream().write(html);
		}
		return true;
	}

	private String readFileAsString(WebzFile file) throws IOException, WebzException {

		WebzFileDownloader downloader = file.getFileDownloader();
		if (downloader == null) {
			// should not happen - otherwise metadata would have been null
			return null;
		}
		return new FileDownloaderWithBOM(downloader, defaultEncoding).getContentAsStringAndClose();
	}

}
