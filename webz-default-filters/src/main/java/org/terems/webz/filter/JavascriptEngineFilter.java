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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzMetadata;
import org.terems.webz.WebzProperties;
import org.terems.webz.base.BaseWebzFilter;
import org.terems.webz.config.GeneralAppConfig;
import org.terems.webz.config.JavascriptEngineConfig;
import org.terems.webz.filter.helpers.FileDownloaderWithBOM;
import org.terems.webz.util.WebzUtils;

public class JavascriptEngineFilter extends BaseWebzFilter {

	public static final String SCRIPT_ENGINE_NAME = "nashorn";

	private String defaultEncoding;
	private Set<String> fileSuffixesLowerCased;
	private boolean processFolders;
	private String resultingMimetype;

	private Collection<CompiledScript> compiledScripts;
	private ThreadLocal<ScriptContext> scriptContext = new ThreadLocal<ScriptContext>();

	@Override
	public void init(WebzContext context) throws IOException, WebzException {

		GeneralAppConfig generalConfig = getAppConfig().getConfigObject(GeneralAppConfig.class);
		JavascriptEngineConfig jsEngineConfig = getAppConfig().getConfigObject(JavascriptEngineConfig.class);

		defaultEncoding = generalConfig.getDefaultEncoding();
		fileSuffixesLowerCased = jsEngineConfig.getFileSuffixesLowerCased();
		processFolders = jsEngineConfig.getProcessFolders();
		resultingMimetype = jsEngineConfig.getResultingMimetype();

		ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName(SCRIPT_ENGINE_NAME);
		if (!(scriptEngine instanceof Compilable)) {
			throw new WebzException("'" + SCRIPT_ENGINE_NAME + "' ScriptEngine does not implement Compilable interface");
		}
		compileScripts(context, (Compilable) scriptEngine);
	}

	private void compileScripts(WebzContext context, Compilable compilable) throws IOException, WebzException {

		WebzFile jsLibsFolder = context.getFile(WebzProperties.WEBZ_JS_LIBS_FOLDER);
		WebzFile jsTxtFile = jsLibsFolder.getDescendant(WebzProperties.JS_TXT_FILE);

		WebzFileDownloader jsTxtDownloader = jsTxtFile.getFileDownloader();
		if (jsTxtDownloader == null) {
			throw new WebzException("'" + jsTxtFile.getPathname() + "' was not found or is not a file");
		}
		try {
			// TODO move window object initialization to a js file
			scriptEngine.eval("var window={};", scriptContext);

			String[] jsLibs = new FileDownloaderWithBOM(jsTxtDownloader, defaultEncoding).getContentAsStringAndClose().split("\\s+");
			for (String jsLib : jsLibs) {

				// TODO make sure js libs loading never fails with NullPointerException
				FileDownloaderWithBOM libDownoaderWithBOM = new FileDownloaderWithBOM(
						jsLibsFolder.getDescendant(jsLib).getFileDownloader(), defaultEncoding);

				// TODO switch from String to Stream
				scriptEngine.eval(libDownoaderWithBOM.getContentAsStringAndClose(), scriptContext);
			}
		} catch (ScriptException e) {
			throw new WebzException(e);
		}
	}

	@Override
	public void serve(HttpServletRequest req, HttpServletResponse resp, WebzChainContext chainContext) throws IOException, WebzException {

		if (!runJavascriptEngine(req, resp, chainContext)) {
			chainContext.nextPlease(req, resp);
		}
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

	private boolean runJavascriptEngine(HttpServletRequest req, HttpServletResponse resp, WebzContext context) throws IOException,
			WebzException {

		if (req.getParameterMap().containsKey(QUERY_PARAM_RAW)) {
			// ?raw => give up in favor of StaticContentFilter
			return false;
		}

		WebzFile file = context.resolveFile(req);
		if (isForJavascriptEngine(file)) {
			return false;
		}

		String html = executeJavascript(context);
		byte[] htmlBytes = html.getBytes(defaultEncoding);

		WebzUtils.prepareStandardHeaders(resp, resultingMimetype, defaultEncoding, htmlBytes.length);
		if (!WebzUtils.isHttpMethodHead(req)) {

			resp.getOutputStream().write(htmlBytes, 0, htmlBytes.length);
		}
		return true;
	}

	private String executeJavascript(WebzContext context) throws IOException, WebzException {

		ScriptEngine scriptEngine = scriptEngineManager.get().getEngineByName(SCRIPT_ENGINE_NAME);

		loadAllWebzJsLibs(scriptEngine, context, scriptContext);
		try {
			return WebzUtils.assertString(scriptEngine.eval(WEBZ_TEMPLATE_ADAPTER_JS_FUNCTION + "(" + CONTENT_TEMPLATE_JS_VAR + ","
					+ PAGE_CONTEXT_JS_VAR + ");", scriptContext));
			// return WebzUtils.assertString(scriptEngine.eval("(function(contentTemplate,pageScope){return "
			// + WEBZ_TEMPLATE_ADAPTER_JS_FUNCTION + "(contentTemplate, pageScope);})(" + CONTENT_TEMPLATE_JS_VAR + ","
			// + PAGE_SCOPE_JS_VAR + ");", scriptContext));
		} catch (ScriptException e) {
			throw new WebzException(e);
		}
	}

	private ScriptContext prepareScriptContext() {

		// TODO cache scriptContext
		ScriptContext scriptContext = new SimpleScriptContext();

		Map<String, Object> jsVars = new HashMap<String, Object>();
		jsVars.put(CONTENT_TEMPLATE_JS_VAR, template);
		scriptContext.setBindings(new SimpleBindings(jsVars), ScriptContext.ENGINE_SCOPE);
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
