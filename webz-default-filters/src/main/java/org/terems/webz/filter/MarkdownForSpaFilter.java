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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
	public static final String IS_FOLDER_MUSTACHE_VAR = "IS-FOLDER";
	// TODO private static final String SHORTEST_URI_MUSTACHE_VAR = "SHORTEST-URI";
	// TODO private static final String SHORTEST_RELATIVE_URI_MUSTACHE_VAR = "SHORTEST-RELATIVE-URI";
	// TODO private static final String SHORTEST_URL_MUSTACHE_VAR = "SHORTEST-URL";
	public static final String FULL_URI_MUSTACHE_VAR = "FULL-URI";
	public static final String FULL_RELATIVE_URI_MUSTACHE_VAR = "FULL-RELATIVE-URI";
	// TODO private static final String FULL_URL_MUSTACHE_VAR = "FULL-URL";

	public static final String WEBZ_ROOT_MUSTACHE_VAR = "WEBZ-ROOT";
	public static final String WEBZ_BREADCRUMBS_MUSTACHE_VAR = "WEBZ-BREADCRUMBS";
	public static final String WEBZ_FOLDER_INDEX_MUSTACHE_VAR = "WEBZ-FOLDER-INDEX";
	public static final String SUBFOLDERS_MUSTACHE_VAR = "SUBFOLDERS";
	public static final String SUBFILES_MUSTACHE_VAR = "SUBFILES";

	public static final String ALL_MUSTACHE_VAR = "ALL";
	public static final String IS_NOT_EMPTY_MUSTACHE_VAR = "IS-NOT-EMPTY";

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
		if (metadata == null || !(metadata.isFolder() || WebzUtils.toLowerCaseEng(metadata.getName()).endsWith(markdownSuffixLowerCased))) {
			return false;
		}

		String mainContent = null;

		if (metadata.isFile()) {
			String markdownContent = readFileAsString(file);
			if (markdownContent == null) {
				return false;
			}
			mainContent = pegDownProcessor.get().markdownToHtml(markdownContent, linkRenderer);
		}

		// TODO parse supplementary JSON files

		Map<String, Object> pageScope = populatePageScope(file, context);
		if (mainContent != null) {
			// // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ //
			pageScope.put(MAIN_CONTENT_MUSTACHE_VAR, mainContent);
			// \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\
		}

		WebzByteArrayOutputStream html = executeMustache(new Object[] { pageScope }, context);

		WebzUtils.prepareStandardHeaders(resp, mustacheResultingMimetype, defaultEncoding, html.size());
		if (!WebzUtils.isHttpMethodHead(req)) {

			resp.getOutputStream().write(html.getInternalByteArray(), 0, html.size());
		}
		return true;
	}

	private Map<String, Object> populatePageScope(WebzFile file, WebzContext context) throws IOException, WebzException {

		Map<String, Object> pageScope = new HashMap<String, Object>();

		pageScope.put(WEBZ_FILE_MUSTACHE_VAR, populateWebzFileMap(file, context));

		WebzMetadata metadata = file.getMetadata();
		if (metadata.isFolder()) {

			Map<String, Object> webzChildren = populateFolderIndex(file, context);
			if (webzChildren != null) {
				pageScope.put(WEBZ_FOLDER_INDEX_MUSTACHE_VAR, webzChildren);
			}
		}

		pageScope.put(WEBZ_ROOT_MUSTACHE_VAR, populateWebzFileMap(context.getFile(null), context));
		pageScope.put(WEBZ_BREADCRUMBS_MUSTACHE_VAR, populateWebzBreadcrumbs(file, context));

		WebzFile parent = file.getParent();
		if (parent != null && !metadata.isFolder()) {

			Map<String, Object> webzSiblings = populateFolderIndex(parent, context);
			if (webzSiblings != null) {
				pageScope.put(WEBZ_FOLDER_INDEX_MUSTACHE_VAR, webzSiblings);
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
			if (metadata.isFolder()) {
				webzFile.put(IS_FOLDER_MUSTACHE_VAR, Boolean.TRUE);
				webzFile.put(FULL_RELATIVE_URI_MUSTACHE_VAR, metadata.getName() + '/');
			} else {
				webzFile.put(FULL_RELATIVE_URI_MUSTACHE_VAR, metadata.getName());
			}
			// TODO implement relative URIs more properly
		}
		webzFile.put(PATHNAME_MUSTACHE_VAR, pathname);
		webzFile.put(FULL_URI_MUSTACHE_VAR, context.resolveUri(file));

		return webzFile;
	}

	private Map<String, Object> populateFolderIndex(WebzFile file, WebzContext context) throws IOException, WebzException {

		Collection<WebzFile> children = file.listChildren(false);
		if (children == null) {
			return null;
		}

		Map<String, Collection<Object>> webzSubfolders = new HashMap<String, Collection<Object>>();
		Map<String, Collection<Object>> webzSubfiles = new HashMap<String, Collection<Object>>();
		Collection<Object> webzAllChildren = new LinkedList<Object>();

		Map<String, Map<String, Object>> isNotEmpty = new HashMap<String, Map<String, Object>>();
		Map<String, Object> isNotEmptySubfolders = new HashMap<String, Object>();
		Map<String, Object> isNotEmptySubfiles = new HashMap<String, Object>();

		Map<String, Object> webzFolderIndex = new HashMap<String, Object>(children.size());
		for (WebzFile child : children) {

			WebzMetadata childMetadata = child.getMetadata();
			if (childMetadata != null) {

				boolean isChildFolder = childMetadata.isFolder();
				Map<String, Collection<Object>> webzSubitems = isChildFolder ? webzSubfolders : webzSubfiles;
				Map<String, Object> isNotEmptySubitems = isChildFolder ? isNotEmptySubfolders : isNotEmptySubfiles;

				Map<String, Object> webzChild = populateWebzFileMap(child, context);
				webzAllChildren.add(webzChild);

				populateChildAgainstOrigin(webzSubitems, isNotEmptySubitems, webzChild, ALL_MUSTACHE_VAR);
				for (String originName : childMetadata.getOriginNames()) {
					populateChildAgainstOrigin(webzSubitems, isNotEmptySubitems, webzChild, originName);
				}
			}
		}
		if (!webzAllChildren.isEmpty()) {

			webzFolderIndex.put(ALL_MUSTACHE_VAR, webzAllChildren);
			webzFolderIndex.put(IS_NOT_EMPTY_MUSTACHE_VAR, isNotEmpty);

			if (!isNotEmptySubfolders.isEmpty()) {
				isNotEmpty.put(SUBFOLDERS_MUSTACHE_VAR, isNotEmptySubfolders);
			}
			if (!isNotEmptySubfiles.isEmpty()) {
				isNotEmpty.put(SUBFILES_MUSTACHE_VAR, isNotEmptySubfiles);
			}
		}
		if (!webzSubfolders.isEmpty()) {
			webzFolderIndex.put(SUBFOLDERS_MUSTACHE_VAR, webzSubfolders);
		}
		if (!webzSubfiles.isEmpty()) {
			webzFolderIndex.put(SUBFILES_MUSTACHE_VAR, webzSubfiles);
		}

		return webzFolderIndex;
	}

	private void populateChildAgainstOrigin(Map<String, Collection<Object>> webzSubitems, Map<String, Object> isNotEmptySubitems,
			Map<String, Object> webzChild, String originName) {

		Collection<Object> webzChildren = webzSubitems.get(originName);
		if (webzChildren == null) {
			webzChildren = new LinkedList<Object>();
			webzSubitems.put(originName, webzChildren);
			isNotEmptySubitems.put(originName, Boolean.TRUE);
		}
		webzChildren.add(webzChild);
	}

	private Collection<Object> populateWebzBreadcrumbs(WebzFile file, WebzContext context) throws IOException, WebzException {

		List<Object> breadcrumbs = new LinkedList<Object>();
		WebzFile parent = file.getParent();

		while (parent != null && parent.getParent() != null) {

			breadcrumbs.add(populateWebzFileMap(parent, context));
			parent = parent.getParent();
		}
		Collections.reverse(breadcrumbs);

		return breadcrumbs;
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
