package org.terems.webz.servlet;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzEngine;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFileSystem;
import org.terems.webz.impl.WebzEngineMain;
import org.terems.webz.impl.dropbox.DropboxFileSystem;
import org.terems.webz.obsolete.ObsoleteWebzEngine;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxRequestConfig;

@SuppressWarnings("serial")
public class WebzHttpServletEnvelope extends HttpServlet {

	private static Logger LOG = LoggerFactory.getLogger(WebzHttpServletEnvelope.class);

	private String dbxBasePath;
	private String dbxAccessToken;

	private String dbxClientDisplayName;
	private String dbxClientVersion;

	private WebzEngine webzEngine;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException {

		if (webzEngine == null) {
			initWebzEngine();
		}

		// TODO leverage from "last modified" http logic supported in parent implementation ?
		webzEngine.service(req, resp);

	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		dbxBasePath = config.getInitParameter("dbxBasePath");
		dbxAccessToken = config.getInitParameter("dbxAccessToken");

		dbxClientDisplayName = config.getInitParameter("dbxClientDisplayName");
		dbxClientVersion = config.getInitParameter("dbxClientVersion");
	}

	private Object webzEngineMutex = new Object();

	private void initWebzEngine() throws ServletException {

		LOG.info("INITIALIZING WEBZ ENGINE...");
		synchronized (webzEngineMutex) {

			if (webzEngine == null) {
				String dbxClientId = getRidOfWhitespacesSafely(dbxClientDisplayName) + "/"
						+ getRidOfWhitespacesSafely(dbxClientVersion);
				DbxRequestConfig dbxConfig = new DbxRequestConfig(dbxClientId, Locale.getDefault().toString());

				LOG.info("Dropbox client ID that will be used: '" + dbxConfig.clientIdentifier + "' (locale: '"
						+ dbxConfig.userLocale + "')");

				try {
					WebzFileSystem dropboxFileSource = new DropboxFileSystem(new DbxClient(dbxConfig, dbxAccessToken),
							dbxBasePath);

					webzEngine = new WebzEngineMain(dropboxFileSource, new ObsoleteWebzEngine());

				} catch (IOException | WebzException e) {
					throw new ServletException(e);
				}

				LOG.info("FINISHED INITIALIZING WEBZ ENGINE");
			} else {
				LOG.info("WEBZ ENGINE WAS ALREADY INITIALIZED - NO NEED TO INITIALIZE AGAIN");
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
