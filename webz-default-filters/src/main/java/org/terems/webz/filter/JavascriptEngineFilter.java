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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzMetadata;
import org.terems.webz.WebzProperties;
import org.terems.webz.WebzReaderDownloader;
import org.terems.webz.base.BaseWebzFilter;
import org.terems.webz.config.GeneralAppConfig;
import org.terems.webz.config.JavascriptEngineConfig;
import org.terems.webz.util.WebzUtils;

public class JavascriptEngineFilter extends BaseWebzFilter {

	private ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("nashorn");
	private Collection<CompiledScript> compiledScripts;

	public static final String WEBZ_JS_CONTEXT_VAR = "webzJsContext";

	public static final String WEBZ_RUN_TEMPLATES_JS_FUNCTION = "webzRunTemplates";

	private String defaultEncoding;
	private Set<String> fileSuffixesLowerCased;
	private boolean processFolders;
	private String resultingMimetype;

	@Override
	public void init(WebzContext context) throws IOException, WebzException {

		GeneralAppConfig generalConfig = getAppConfig().getConfigObject(GeneralAppConfig.class);
		JavascriptEngineConfig jsEngineConfig = getAppConfig().getConfigObject(JavascriptEngineConfig.class);

		defaultEncoding = generalConfig.getDefaultEncoding();
		fileSuffixesLowerCased = jsEngineConfig.getFileSuffixesLowerCased();
		processFolders = jsEngineConfig.getProcessFolders();
		resultingMimetype = jsEngineConfig.getResultingMimetype();

		compileScripts(context);
	}

	private void compileScripts(WebzContext context) throws IOException, WebzException {

		if (!(scriptEngine instanceof Compilable)) {
			throw new WebzException("ScriptEngine does not implement Compilable interface");
		}
		Compilable compilable = (Compilable) scriptEngine;

		WebzFile jsLibsFolder = context.getFile(WebzProperties.WEBZ_JS_LIBS_FOLDER);
		WebzFile jsTxtFile = jsLibsFolder.getDescendant(WebzProperties.JS_TXT_FILE);

		WebzReaderDownloader jsTxtDownloader = jsTxtFile.getFileDownloader();
		if (jsTxtDownloader == null) {
			throw new WebzException("'" + jsTxtFile.getPathname() + "' was not found or is not a file");
		}
		try {
			String[] jsLibs = jsTxtDownloader.getContentAsStringAndClose().split("\\s+");

			compiledScripts = new ArrayList<CompiledScript>(jsLibs.length);

			for (String jsLib : jsLibs) {

				WebzFile jsLibFile = jsLibsFolder.getDescendant(jsLib);
				WebzReaderDownloader jsLibDownloader = jsLibFile.getFileDownloader();

				if (jsLibDownloader == null) {
					throw new WebzException("'" + jsLibFile.getPathname() + "' does not exist or is not a file");
				}
				compiledScripts.add(compilable.compile(jsLibDownloader.getReader()));
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

		ScriptContext scriptContext = initScriptContext(req);
		Bindings engineScope = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);

		// TODO declare this class in a separate file
		engineScope.put(WEBZ_JS_CONTEXT_VAR, new Object() {

			@SuppressWarnings("unused")
			public WebzFile getCurrentFile() throws IOException, WebzException {
				return context.resolveFile(req);
			}

			@SuppressWarnings("unused")
			public WebzFile getFile(String pathInfo) throws IOException, WebzException {
				return context.getFile(pathInfo);
			}

			@SuppressWarnings("unused")
			public String resolveUri(WebzFile file) throws IOException, WebzException {
				return context.resolveUri(file, req);
			}

		});

		String html = executeJavascript(scriptContext);
		byte[] htmlBytes = html.getBytes(defaultEncoding);

		WebzUtils.prepareStandardHeaders(resp, resultingMimetype, defaultEncoding, htmlBytes.length);
		if (!WebzUtils.isHttpMethodHead(req)) {

			resp.getOutputStream().write(htmlBytes, 0, htmlBytes.length);
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

	private ScriptContext initScriptContext(HttpServletRequest req) throws WebzException {

		// TODO cache scriptContext
		ScriptContext scriptContext = new SimpleScriptContext();
		Bindings engineScope = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
		engineScope.put("window", engineScope);

		try {
			for (CompiledScript compiledScript : compiledScripts) {
				compiledScript.eval(scriptContext);
			}
		} catch (ScriptException e) {

			throw new WebzException(e);
		}

		return scriptContext;
	}

	private String executeJavascript(ScriptContext scriptContext) throws IOException, WebzException {

		try {
			return WebzUtils.assertString(scriptEngine.eval(WEBZ_RUN_TEMPLATES_JS_FUNCTION + "()", scriptContext));

		} catch (ScriptException e) {
			throw new WebzException(e);
		}
	}

}
