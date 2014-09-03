package org.terems.webz.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzApp;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFileSystem;
import org.terems.webz.filter.ErrorFilter;
import org.terems.webz.filter.NotFoundFilter;
import org.terems.webz.filter.StaticContentFilter;
import org.terems.webz.filter.WelcomeFilter;
import org.terems.webz.impl.WebzEngine;
import org.terems.webz.impl.dropbox.DropboxFileSystem;
import org.terems.webz.plugin.WebzFilter;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxRequestConfig;

@SuppressWarnings("serial")
public class WebzHttpServletEnvelope extends HttpServlet {
	// TODO concurrency unit tests ?

	private static final Logger LOG = LoggerFactory.getLogger(WebzHttpServletEnvelope.class);

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		try {
			getWebzApp().serve(req, resp);

		} catch (WebzException e) {
			throw new ServletException(e);
		}
	}

	/**
	 * <a href="http://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java">Double-checked locking - Usage in Java</a>
	 **/
	private WebzApp getWebzApp() throws IOException, WebzException {

		WebzApp webzApp = this.webzApp;
		if (webzApp == null) {

			synchronized (webzAppMutex) {

				webzApp = this.webzApp;
				if (webzApp == null) {

					String webzAppName = getServletConfig().getInitParameter("webzAppName");
					LOG.info("INITIALIZING '" + webzAppName + "'...");

					String dbxAccessToken = getServletConfig().getInitParameter("dbxAccessToken");
					String dbxBasePath = getServletConfig().getInitParameter("dbxBasePath");

					// // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ //
					WebzFilter[] filters = { new ErrorFilter(), new WelcomeFilter(), new StaticContentFilter(), new NotFoundFilter() };
					// \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\

					String dbxClientId = getRidOfWhitespacesSafely(getServletConfig().getInitParameter("dbxClientDisplayName")) + "/"
							+ getRidOfWhitespacesSafely(getServletConfig().getInitParameter("dbxClientVersion"));
					DbxRequestConfig dbxConfig = new DbxRequestConfig(dbxClientId, Locale.getDefault().toString());

					LOG.info("Dropbox client ID that will be used: '" + dbxConfig.clientIdentifier + "' (locale: '" + dbxConfig.userLocale
							+ "')");

					WebzFileSystem dbxFileSystem = new DropboxFileSystem(new DbxClient(dbxConfig, dbxAccessToken), dbxBasePath);

					// // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ //
					this.webzApp = webzApp = new WebzEngine(webzAppName, dbxFileSystem, Arrays.asList(filters));
					// \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\

					LOG.info("FINISHED INITIALIZING '" + webzAppName + "'");
				}
			}
		}
		return webzApp;
	}

	private volatile WebzApp webzApp;
	private Object webzAppMutex = new Object();

	@Override
	public void destroy() {

		synchronized (webzAppMutex) {

			WebzApp webzApp = this.webzApp;
			if (webzApp != null) {

				this.webzApp = null;
				webzApp.destroy();
			}
		}
	}

	private String getRidOfWhitespacesSafely(String value) {
		if (value == null) {
			return "null";
		} else {
			return value.trim().replaceAll("\\s+", "-");
		}
	}

}
