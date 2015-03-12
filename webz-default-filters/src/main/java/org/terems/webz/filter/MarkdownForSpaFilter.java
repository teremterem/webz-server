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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.pegdown.LinkRenderer;
import org.pegdown.PegDownProcessor;
import org.terems.webz.WebzByteArrayOutputStream;
import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzMetadata;
import org.terems.webz.base.BaseWebzFilter;
import org.terems.webz.config.GeneralAppConfig;
import org.terems.webz.config.MarkdownForSpaConfig;
import org.terems.webz.filter.helpers.ConfigurableLinkRenderer;
import org.terems.webz.filter.helpers.FileDownloaderWithBOM;
import org.terems.webz.util.WebzUtils;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;
import com.github.mustachejava.MustacheResolver;

public class MarkdownForSpaFilter extends BaseWebzFilter {

	// TODO support browser caching (ETags or "last modified" timestamp stored in cache against request pathInfo ?)

	public static final String WEBZ_FILE_MUSTACHE_VAR = "WEBZ-FILE";

	public static final String NAME_MUSTACHE_VAR = "NAME";
	public static final String PATHNAME_MUSTACHE_VAR = "PATHNAME";
	// TODO private static final String UNDERLYING_SOURCE_MUSTACHE_VAR = "UNDERLYING-SOURCE";
	// TODO private static final String UNDERLYING_SOURCE_ID_MUSTACHE_VAR = "UNDERLYING-SOURCE-ID";
	// TODO private static final String SHORTEST_URI_MUSTACHE_VAR = "SHORTEST-URI";
	// TODO private static final String SHORTEST_RELATIVE_URI_MUSTACHE_VAR = "SHORTEST-RELATTIVE-URI";
	// TODO private static final String SHORTEST_URL_MUSTACHE_VAR = "SHORTEST-URL";
	public static final String FULL_URI_MUSTACHE_VAR = "FULL-URI";
	// TODO private static final String FULL_RELATIVE_URI_MUSTACHE_VAR = "FULL-RELATTIVE-URI";
	// TODO private static final String FULL_URL_MUSTACHE_VAR = "FULL-URL";

	public static final String WEBZ_PARENT_MUSTACHE_VAR = "WEBZ-PARENT";
	public static final String WEBZ_SIBLINGS_MUSTACHE_VAR = "WEBZ-SIBLINGS";
	// TODO private static final String WEBZ_LINKED_SIBLINGS_MUSTACHE_VAR = "WEBZ-LINKED-SIBLINGS";

	public static final String MAIN_CONTENT_MUSTACHE_VAR = "MAIN-CONTENT";

	private String defaultEncoding;
	private String markdownSuffixLowerCased;
	private String mustacheTemplate;
	private String mustacheResultingMimetype;

	private LinkRenderer linkRenderer;

	private ThreadLocal<PegDownProcessor> pegDownProcessor = new ThreadLocal<PegDownProcessor>() {
		@Override
		protected PegDownProcessor initialValue() {
			return new PegDownProcessor();
		}
	};

	@Override
	public void init() throws WebzException {

		GeneralAppConfig generalConfig = getAppConfig().getAppConfigObject(GeneralAppConfig.class);
		MarkdownForSpaConfig markdownForSpaConfig = getAppConfig().getAppConfigObject(MarkdownForSpaConfig.class);

		defaultEncoding = generalConfig.getDefaultEncoding();
		markdownSuffixLowerCased = markdownForSpaConfig.getMarkdownSuffixLowerCased();
		mustacheTemplate = markdownForSpaConfig.getMustacheTemplate();
		mustacheResultingMimetype = markdownForSpaConfig.getMustacheResultingMimetype();

		linkRenderer = new ConfigurableLinkRenderer(getAppConfig());
		// TODO make ConfigurableLinkRenderer configurable
	}

	@Override
	public void serve(HttpServletRequest req, HttpServletResponse resp, WebzChainContext chainContext) throws IOException, WebzException {

		if (!serveMarkdown(req, resp, chainContext)) {
			chainContext.nextPlease(req, resp);
		}
	}

	private boolean serveMarkdown(HttpServletRequest req, HttpServletResponse resp, WebzContext context) throws IOException, WebzException {

		if (req.getParameterMap().containsKey(QUERY_PARAM_RAW)) {
			// ?raw => give up in favor of StaticContentFilter
			return false;
		}

		WebzFile file = context.resolveFile(req);
		WebzMetadata metadata = file.getMetadata();
		if (metadata == null || !metadata.isFile() || !WebzUtils.toLowerCaseEng(metadata.getName()).endsWith(markdownSuffixLowerCased)) {
			return false;
		}

		String markdownContent = readFileAsString(file);
		if (markdownContent == null) {
			return false;
		}

		String mainContent = pegDownProcessor.get().markdownToHtml(markdownContent, linkRenderer);

		// TODO parse supplementary JSON files

		Map<String, Object> pageScope = populatePageScope(mainContent, file, context);

		WebzByteArrayOutputStream html = executeMustache(new Object[] { pageScope }, context);

		WebzUtils.prepareStandardHeaders(resp, mustacheResultingMimetype, defaultEncoding, html.size());
		if (!WebzUtils.isHttpMethodHead(req)) {

			resp.getOutputStream().write(html.getInternalByteArray(), 0, html.size());
		}
		return true;
	}

	private Map<String, Object> populatePageScope(String mainContent, WebzFile file, WebzContext context) throws IOException, WebzException {

		Map<String, Object> pageScope = new HashMap<String, Object>();

		// // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ //
		pageScope.put(MAIN_CONTENT_MUSTACHE_VAR, mainContent);
		// \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\

		pageScope.put(WEBZ_FILE_MUSTACHE_VAR, populateWebzFileMap(file, context));
		WebzFile parent = file.getParent();
		if (parent != null) {
			pageScope.put(WEBZ_PARENT_MUSTACHE_VAR, populateWebzFileMap(parent, context));

			Collection<WebzFile> children = parent.listChildren();
			if (children != null) {

				Collection<Object> webzSiblings = new ArrayList<Object>(children.size());
				for (WebzFile sibling : children) {
					webzSiblings.add(populateWebzFileMap(sibling, context));
				}
				pageScope.put(WEBZ_SIBLINGS_MUSTACHE_VAR, webzSiblings);
			}
		}

		return pageScope;
	}

	private Map<String, Object> populateWebzFileMap(WebzFile file, WebzContext context) throws IOException, WebzException {

		Map<String, Object> webzFile = new HashMap<String, Object>();

		String pathname = file.getPathname();
		WebzMetadata metadata = file.getMetadata();
		if (metadata != null) {
			webzFile.put(NAME_MUSTACHE_VAR, metadata.getName());

			String linkedPathname = metadata.getLinkedPathname();
			if (linkedPathname != null) {
				pathname = linkedPathname;
			}
		}
		webzFile.put(PATHNAME_MUSTACHE_VAR, pathname);
		webzFile.put(FULL_URI_MUSTACHE_VAR, context.resolveUri(file));

		return webzFile;
	}

	private WebzByteArrayOutputStream executeMustache(Object[] scopes, WebzContext context) throws IOException {

		Mustache mustache = newMustacheFactory(context).compile(mustacheTemplate);

		WebzByteArrayOutputStream result = new WebzByteArrayOutputStream();
		Writer writer = new OutputStreamWriter(result, defaultEncoding);

		mustache.execute(writer, scopes);
		writer.close();

		return result;
	}

	private DefaultMustacheFactory newMustacheFactory(final WebzContext context) {

		return new DefaultMustacheFactory(new MustacheResolver() {
			@Override
			public Reader getReader(String resourceName) {

				FileDownloaderWithBOM downloaderWithBom = null;
				try {
					WebzFileDownloader downloader = context.getFile(resourceName).getFileDownloader();
					if (downloader == null) {
						throw new WebzException("'" + resourceName
								+ "' mustache template was not found (or a folder of the same name was found instead)");
					}

					downloaderWithBom = new FileDownloaderWithBOM(downloader, defaultEncoding);
					return new InputStreamReader(downloaderWithBom.content, downloaderWithBom.actualEncoding);

				} catch (IOException e) {
					new MustacheException(e);
				} catch (WebzException e) {
					new MustacheException(e);
				}
				return null; // dead code
			}
		});
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
