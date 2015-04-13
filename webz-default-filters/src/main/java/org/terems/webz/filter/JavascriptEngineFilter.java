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
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzContext;
import org.terems.webz.WebzDefaults;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzMetadata;
import org.terems.webz.base.BaseWebzFilter;
import org.terems.webz.config.GeneralAppConfig;
import org.terems.webz.config.JavascriptEngineConfig;
import org.terems.webz.config.MimetypesConfig;
import org.terems.webz.filter.helpers.JavascriptEnginePool;
import org.terems.webz.util.WebzUtils;

public class JavascriptEngineFilter extends BaseWebzFilter {

	public static final int NUMBER_OF_ENGINES = 3;
	// TODO make number of javascript engines parameter configurable

	private String defaultEncoding;
	private Set<String> fileSuffixesLowerCased;
	private boolean processFolders;
	private String pageDefaultMimetype;
	private String jsonMimetype;

	private JavascriptEnginePool enginePool;

	@Override
	public void init(WebzContext context) throws IOException, WebzException {

		JavascriptEngineConfig jsEngineConfig = getAppConfig().getConfigObject(JavascriptEngineConfig.class);
		fileSuffixesLowerCased = jsEngineConfig.getFileSuffixesLowerCased();
		processFolders = jsEngineConfig.getProcessFolders();
		pageDefaultMimetype = jsEngineConfig.getPageDefaultMimetype();

		defaultEncoding = getAppConfig().getConfigObject(GeneralAppConfig.class).getDefaultEncoding();
		jsonMimetype = getAppConfig().getConfigObject(MimetypesConfig.class).getMimetype("json", WebzDefaults.JSON_MIMETYPE);

		enginePool = new JavascriptEnginePool(NUMBER_OF_ENGINES, context);
	}

	@Override
	public void serve(HttpServletRequest req, HttpServletResponse resp, WebzChainContext chainContext) throws IOException, WebzException {

		if (!runJavascriptEngine(req, resp, chainContext)) {
			chainContext.nextPlease(req, resp);
		}
	}

	private boolean runJavascriptEngine(final HttpServletRequest req, HttpServletResponse resp, final WebzContext context)
			throws IOException, WebzException {

		if (req.getParameterMap().containsKey(QUERY_PARAM_RAW)) {
			// ?raw => give up in favor of StaticContentFilter
			return false;
		}

		WebzFile file = context.resolveFile(req);
		if (!isForJavascriptEngine(file)) {
			return false;
		}

		String content;
		String mimetype;
		if (req.getParameterMap().containsKey(QUERY_PARAM_PAGE_CONTEXT)) {
			content = enginePool.invokePreparePageContextFunc(req, context);
			mimetype = jsonMimetype;
		} else {
			JavascriptEnginePool.PageContent pageContent = enginePool.invokeRenderPageFunc(req, context);
			content = pageContent.content;
			mimetype = pageContent.mimetype == null ? pageDefaultMimetype : pageContent.mimetype;
		}
		byte[] contentBytes = content.getBytes(defaultEncoding);

		WebzUtils.prepareStandardHeaders(resp, mimetype, defaultEncoding, contentBytes.length);
		if (!WebzUtils.isHttpMethodHead(req)) {

			resp.getOutputStream().write(contentBytes);
		}
		return true;
	}

	private boolean isForJavascriptEngine(WebzFile file) throws IOException, WebzException {

		WebzMetadata metadata = file.getMetadata();
		if (metadata != null) {

			if (processFolders && metadata.isFolder()) {
				return true;
			}
			String nameLowerCased = WebzUtils.toLowerCaseEng(metadata.getName());
			for (String fileSuffixLowerCased : fileSuffixesLowerCased) {

				if (nameLowerCased.endsWith(fileSuffixLowerCased)) {
					return true;
				}
			}
		}
		return false;
	}

}
