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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
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
	public static final String EXT_IS_MUSTACHE_VAR = "EXT-IS";
	public static final String FULL_URI_MUSTACHE_VAR = "FULL-URI";

	public static final String WEBZ_ROOT_MUSTACHE_VAR = "WEBZ-ROOT";
	public static final String WEBZ_BREADCRUMBS_MUSTACHE_VAR = "WEBZ-BREADCRUMBS";

	public static final String WEBZ_FOLDER_MUSTACHE_VAR = "WEBZ-FOLDER";
	public static final String SUBFOLDERS_MUSTACHE_VAR = "SUBFOLDERS";
	public static final String SUBFILES_MUSTACHE_VAR = "SUBFILES";

	public static final String ALL_MUSTACHE_VAR = "ALL";
	public static final String NOT_EMPTY_MUSTACHE_VAR = "NOT-EMPTY";
	public static final String INDEX_MUSTACHE_VAR = "INDEX";
	public static final String REVERSE_INDEX_MUSTACHE_VAR = "REVERSE-INDEX";
	public static final String LIST_MUSTACHE_VAR = "LIST";
	public static final String REVERSE_LIST_MUSTACHE_VAR = "REVERSE-LIST";

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

	private boolean isMarkdown(String name) {
		return WebzUtils.toLowerCaseEng(name).endsWith(markdownSuffixLowerCased);
	}

	private boolean serveMarkdown(HttpServletRequest req, HttpServletResponse resp, WebzContext context) throws IOException, WebzException {

		if (req.getParameterMap().containsKey(QUERY_PARAM_RAW)) {
			// ?raw => give up in favor of StaticContentFilter
			return false;
		}

		WebzFile file = context.resolveFile(req);
		WebzMetadata metadata = file.getMetadata();
		if (metadata == null || !(metadata.isFolder() || isMarkdown(metadata.getName()))) {
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

		Map<String, Object> pageScope = populatePageScope(file, req, context);
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

	private Map<String, Object> populatePageScope(WebzFile file, HttpServletRequest req, WebzContext context) throws IOException,
			WebzException {

		Map<String, Object> pageScope = new HashMap<String, Object>();

		pageScope.put(WEBZ_FILE_MUSTACHE_VAR, populateWebzFileMap(file, req, context));

		WebzMetadata metadata = file.getMetadata();
		if (metadata.isFolder()) {

			Map<String, Object> webzChildren = populateFolderIndex(file, req, context);
			if (webzChildren != null) {
				pageScope.put(WEBZ_FOLDER_MUSTACHE_VAR, webzChildren);
			}
		}

		pageScope.put(WEBZ_ROOT_MUSTACHE_VAR, populateWebzFileMap(context.getFile(null), req, context));
		pageScope.put(WEBZ_BREADCRUMBS_MUSTACHE_VAR, populateWebzBreadcrumbs(file, req, context));

		WebzFile parent = file.getParent();
		if (parent != null && !metadata.isFolder()) {

			Map<String, Object> webzSiblings = populateFolderIndex(parent, req, context);
			if (webzSiblings != null) {
				pageScope.put(WEBZ_FOLDER_MUSTACHE_VAR, webzSiblings);
			}
		}

		return pageScope;
	}

	private Map<String, Object> populateWebzFileMap(WebzFile file, HttpServletRequest req, WebzContext context) throws IOException,
			WebzException {

		Map<String, Object> webzFile = new HashMap<String, Object>();

		String pathname = file.getPathname();
		WebzMetadata metadata = file.getMetadata();
		if (metadata != null) {
			webzFile.put(NAME_MUSTACHE_VAR, metadata.getName());

			String linkedPathname = metadata.getLinkedPathname();
			if (linkedPathname != null) {
				pathname = linkedPathname;
			}

			String fileExtension = WebzUtils.getFileExtension(metadata);
			if (fileExtension != null) {
				webzFile.put(EXT_IS_MUSTACHE_VAR, Collections.singletonMap(WebzUtils.toLowerCaseEng(fileExtension), Boolean.TRUE));
			}
			if (metadata.isFolder()) {
				webzFile.put(IS_FOLDER_MUSTACHE_VAR, Boolean.TRUE);
			}
		}
		webzFile.put(PATHNAME_MUSTACHE_VAR, pathname);
		webzFile.put(FULL_URI_MUSTACHE_VAR, context.resolveUri(file, req));

		return webzFile;
	}

	private Map<String, Object> populateFolderIndex(WebzFile file, HttpServletRequest req, WebzContext context) throws IOException,
			WebzException {

		Collection<WebzFile> children = file.listChildren(false);
		if (children == null) {
			return null;
		}

		Map<String, Collection<Object>> webzSubfolders = new HashMap<String, Collection<Object>>();
		Map<String, Collection<Object>> webzSubfiles = new HashMap<String, Collection<Object>>();
		Collection<Object> webzAllChildren = new LinkedList<Object>();

		Map<String, Map<String, Object>> notEmpty = new HashMap<String, Map<String, Object>>();
		Map<String, Object> notEmptySubfolders = new HashMap<String, Object>();
		Map<String, Object> notEmptySubfiles = new HashMap<String, Object>();
		Map<String, Object> notEmptyAll = new HashMap<String, Object>();

		Map<String, Object> webzFolderIndex = new HashMap<String, Object>(children.size());
		for (WebzFile child : children) {

			WebzMetadata childMetadata = child.getMetadata();
			if (childMetadata != null) {

				boolean isChildFolder = childMetadata.isFolder();
				Map<String, Collection<Object>> webzSubitems = isChildFolder ? webzSubfolders : webzSubfiles;
				Map<String, Object> isNotEmptySubitems = isChildFolder ? notEmptySubfolders : notEmptySubfiles;

				Map<String, Object> webzChild = populateWebzFileMap(child, req, context);
				webzAllChildren.add(webzChild);

				putChildAgainstOrigin(webzChild, webzSubitems, isNotEmptySubitems, notEmptyAll, ALL_MUSTACHE_VAR);
				for (String origin : childMetadata.getOrigins()) {
					putChildAgainstOrigin(webzChild, webzSubitems, isNotEmptySubitems, notEmptyAll, origin);
				}
			}
		}
		if (!webzAllChildren.isEmpty()) {

			webzFolderIndex.put(ALL_MUSTACHE_VAR, webzAllChildren);
			webzFolderIndex.put(NOT_EMPTY_MUSTACHE_VAR, notEmpty);

			if (!notEmptySubfolders.isEmpty()) {
				notEmpty.put(SUBFOLDERS_MUSTACHE_VAR, notEmptySubfolders);
			}
			if (!notEmptySubfiles.isEmpty()) {
				notEmpty.put(SUBFILES_MUSTACHE_VAR, notEmptySubfiles);
			}
			if (!notEmptyAll.isEmpty()) {
				notEmpty.put(ALL_MUSTACHE_VAR, notEmptyAll);
			}
		}
		if (!webzSubfolders.isEmpty()) {
			webzFolderIndex.put(SUBFOLDERS_MUSTACHE_VAR, webzSubfolders);
		}
		if (!webzSubfiles.isEmpty()) {
			webzFolderIndex.put(SUBFILES_MUSTACHE_VAR, webzSubfiles);
		}

		return webzFolderIndex.isEmpty() ? null : webzFolderIndex;
	}

	private void putChildAgainstOrigin(Map<String, Object> webzChild, Map<String, Collection<Object>> webzSubitems,
			Map<String, Object> isNotEmptySubitems, Map<String, Object> isNotEmptyAll, String origin) {

		Collection<Object> webzChildren = webzSubitems.get(origin);
		if (webzChildren == null) {

			webzChildren = new LinkedList<Object>();
			webzSubitems.put(origin, webzChildren);

			isNotEmptySubitems.put(origin, Boolean.TRUE);
			isNotEmptyAll.put(origin, Boolean.TRUE);
		}
		webzChildren.add(webzChild);
	}

	private Map<String, Object> populateWebzBreadcrumbs(WebzFile file, HttpServletRequest req, WebzContext context) throws IOException,
			WebzException {

		List<Map<String, Object>> reverseList = new LinkedList<Map<String, Object>>();
		Map<String, Object> reverseIndex = new HashMap<String, Object>();

		// current file is stored in WEBZ-FILE var separately from WEBZ-BREADCRUMBS - skip
		file = file.getParent();
		for (int i = 0; file != null; i++) {

			WebzFile parent = file.getParent();
			if (parent == null) {
				// root folder is stored in WEBZ-ROOT var separately from WEBZ-BREADCRUMBS - skip
				break;
			}

			Map<String, Object> webzFile = populateWebzFileMap(file, req, context);
			reverseList.add(webzFile);
			reverseIndex.put(String.valueOf(i), webzFile);

			file = parent;
		}

		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>(reverseList.size());
		Map<String, Object> index = new HashMap<String, Object>();

		ListIterator<Map<String, Object>> it = list.listIterator();
		for (int i = 0; it.hasPrevious(); i++) {

			Map<String, Object> webzFile = it.previous();
			list.add(webzFile);
			index.put(String.valueOf(i), webzFile);
		}

		Map<String, Object> breadcrumbs = new HashMap<String, Object>();
		breadcrumbs.put(LIST_MUSTACHE_VAR, list);
		breadcrumbs.put(REVERSE_LIST_MUSTACHE_VAR, reverseList);
		breadcrumbs.put(INDEX_MUSTACHE_VAR, index);
		breadcrumbs.put(REVERSE_INDEX_MUSTACHE_VAR, reverseIndex);

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
