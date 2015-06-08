package org.terems.webz.filter.helpers;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzProperties;
import org.terems.webz.WebzReaderDownloader;
import org.terems.webz.util.WebzUtils;

public class JavascriptEnginePool {

	private static final Logger LOG = LoggerFactory.getLogger(JavascriptEnginePool.class);

	public static final String WEBZ_INIT_JS_FUNCTION = "webzInit";
	public static final String WEBZ_PREPARE_PAGE_CONTEXT_JS_FUNCTION = "webzPreparePageContext";
	public static final String WEBZ_RENDER_PAGE_JS_FUNCTION = "webzRenderPage";
	// TODO make these javascript function names configurable ?

	public static final String PAGE_CONTENT_JS_FIELD = "content";
	public static final String PAGE_MIMETYPE_JS_FIELD = "mimetype";

	private BlockingQueue<ScriptEngine> enginePool;

	public String invokePreparePageContextFunc(HttpServletRequest req, WebzContext context) throws WebzException {

		ScriptEngine engine = null;
		try {
			engine = enginePool.take();
			Invocable invocableEngine = (Invocable) engine;

			JsWebzContext jsWebzContext = new JsWebzContext(context, req);
			Object rawPageContext = invocableEngine.invokeFunction(WEBZ_PREPARE_PAGE_CONTEXT_JS_FUNCTION, jsWebzContext,
					WebzUtils.getFullUrl(req));
			return WebzUtils.assertString(invocableEngine.invokeMethod(engine.get("JSON"), "stringify", rawPageContext));

		} catch (InterruptedException | NoSuchMethodException | ScriptException e) {
			throw new WebzException(e);

		} finally {
			if (engine != null) {
				enginePool.add(engine);
			}
		}
	}

	public PageContent invokeRenderPageFunc(HttpServletRequest req, WebzContext context) throws WebzException {

		ScriptEngine engine = null;
		try {
			engine = enginePool.take();
			Invocable invocableEngine = (Invocable) engine;

			JsWebzContext jsWebzContext = new JsWebzContext(context, req);
			Object rawPageContext = invocableEngine.invokeFunction(WEBZ_PREPARE_PAGE_CONTEXT_JS_FUNCTION, jsWebzContext);
			return processRawPageContent(invocableEngine.invokeFunction(WEBZ_RENDER_PAGE_JS_FUNCTION, rawPageContext));

		} catch (InterruptedException | NoSuchMethodException | ScriptException e) {
			throw new WebzException(e);

		} finally {
			if (engine != null) {
				enginePool.add(engine);
			}
		}
	}

	public static class PageContent {
		public String mimetype;
		public String content;
	}

	private PageContent processRawPageContent(Object rawPageContent) throws WebzException {

		PageContent pageContent = new PageContent();

		if (rawPageContent instanceof Bindings) {
			Bindings pageContentBindings = (Bindings) rawPageContent;
			pageContent.content = WebzUtils.assertString(pageContentBindings.get(PAGE_CONTENT_JS_FIELD));
			pageContent.mimetype = (String) pageContentBindings.get(PAGE_MIMETYPE_JS_FIELD);
		} else {
			pageContent.content = WebzUtils.assertString(rawPageContent);
		}
		return pageContent;
	}

	private int enginesInited = 0;

	public int getNumOfInitedEngines() {
		return enginesInited;
	}

	public int getNumOfAvailableEngines() {
		return enginePool.size();
	}

	public JavascriptEnginePool(final int numOfEngines, final WebzContext context) throws WebzException {

		if (numOfEngines < 1) {
			throw new WebzException("JavascriptEnginePool should have at least one engine");
		}
		enginePool = new ArrayBlockingQueue<ScriptEngine>(numOfEngines, true);

		final JsWebzFiles jsWebzFiles = new JsWebzFiles(context);

		spawnNewEngine(context, jsWebzFiles, true);
		final int enginesInitedSoFar = 1;
		enginesInited = enginesInitedSoFar;

		if (enginesInitedSoFar == numOfEngines) {
			logEnginesInited(numOfEngines);

		} else {
			Thread backgroundInit = new Thread() {
				@Override
				public void run() {

					for (int i = enginesInited; i < numOfEngines; i++) {

						try {
							spawnNewEngine(context, jsWebzFiles, false);
							enginesInited++;

						} catch (WebzException e) {
							LOG.error("failed to init additional javascript engine(s)\n", e);
						}
					}
					if (enginesInited < numOfEngines) {

						if (LOG.isWarnEnabled()) {
							LOG.warn("only " + enginesInited + " out of " + numOfEngines + " javascript engines were initialized\n");
						}
					} else {
						logEnginesInited(enginesInited);
					}
				}
			};
			backgroundInit.setDaemon(true);
			backgroundInit.start();
		}
	}

	private void logEnginesInited(int numOfEngines) {

		if (LOG.isInfoEnabled()) {
			LOG.info(numOfEngines + " javascript engines were initialized\n");
		}
	}

	private void spawnNewEngine(WebzContext context, JsWebzFiles jsWebzFiles, boolean warnOnNoInitFunction) throws WebzException {

		ScriptEngine engine;
		try {
			engine = createEngineAndLoadScripts(context);

		} catch (IOException e) {
			throw new WebzException(e);
		}
		try {
			((Invocable) engine).invokeFunction(WEBZ_INIT_JS_FUNCTION, jsWebzFiles);

		} catch (NoSuchMethodException e) {
			if (warnOnNoInitFunction && LOG.isWarnEnabled()) {
				LOG.warn(WEBZ_INIT_JS_FUNCTION + "(webzFiles) javascript function is not declared in scripts");
			}

		} catch (ScriptException e) {
			throw new WebzException(e);
		}

		enginePool.add(engine);
	}

	private ScriptEngine createEngineAndLoadScripts(WebzContext context) throws IOException, WebzException {

		ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
		// TODO try --no-java to make nashorn secure:
		// http://stackoverflow.com/questions/20793089/secure-nashorn-js-execution
		if (!(engine instanceof Invocable)) {
			throw new RuntimeException(engine.getClass() + " does not implement " + Invocable.class);
		}

		Bindings engineScope = engine.getBindings(ScriptContext.ENGINE_SCOPE);
		engineScope.put("window", engineScope);

		WebzFile jsListFile = context.getFile(WebzProperties.WEBZ_JS_FILES_TXT_FILE);

		WebzReaderDownloader jsListDownloader = jsListFile.getFileDownloader();
		if (jsListDownloader == null) {
			throw new WebzException("'" + jsListFile.getPathname() + "' was not found or is not a file");
		}

		for (String jsFilePathname : WebzUtils.getTrimmedTxtLines(jsListDownloader.getContentAsStringAndClose())) {

			WebzFile jsFile = context.getFile(jsFilePathname);
			WebzReaderDownloader jsDownloader = jsFile.getFileDownloader();

			if (jsDownloader == null) {
				throw new WebzException("'" + jsFile.getPathname() + "' does not exist or is not a file");
			}
			try {
				engine.eval(jsDownloader.getReader());

			} catch (ScriptException e) {
				throw new WebzException(e);
			} finally {
				jsDownloader.close();
			}
		}

		return engine;
	}

}
