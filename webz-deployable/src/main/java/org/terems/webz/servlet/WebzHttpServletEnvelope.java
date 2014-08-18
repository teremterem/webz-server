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
import org.terems.webz.filter.FileFolderRedirectFilter;
import org.terems.webz.filter.FolderPreloadFilter;
import org.terems.webz.filter.WelcomeFilter;
import org.terems.webz.impl.GenericWebzFile;
import org.terems.webz.impl.WebzEngine;
import org.terems.webz.impl.dropbox.DropboxFileSystem;
import org.terems.webz.obsolete.ObsoleteWebzEngine;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxRequestConfig;

@SuppressWarnings("serial")
public class WebzHttpServletEnvelope extends HttpServlet {

	private static Logger LOG = LoggerFactory.getLogger(WebzHttpServletEnvelope.class);

	private WebzApp webzApp;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException {

		if (webzApp == null) {
			initWebzApp();
		}

		webzApp.service(req, resp);
	}

	private Object webzAppMutex = new Object();

	private void initWebzApp() throws ServletException {

		LOG.info("INITIALIZING ROOT WEBZ APP...");
		synchronized (webzAppMutex) {

			if (webzApp == null) {
				String dbxClientId = getRidOfWhitespacesSafely(getServletConfig().getInitParameter("dbxClientDisplayName")) + "/"
						+ getRidOfWhitespacesSafely(getServletConfig().getInitParameter("dbxClientVersion"));
				DbxRequestConfig dbxConfig = new DbxRequestConfig(dbxClientId, Locale.getDefault().toString());

				LOG.info("Dropbox client ID that will be used: '" + dbxConfig.clientIdentifier + "' (locale: '" + dbxConfig.userLocale
						+ "')");

				try {
					String dbxAccessToken = getServletConfig().getInitParameter("dbxAccessToken");
					String dbxBasePath = "/" + GenericWebzFile.trimFileSeparators(getServletConfig().getInitParameter("dbxBasePath"));

					WebzFileSystem dropboxFileSource = new DropboxFileSystem(new DbxClient(dbxConfig, dbxAccessToken), dbxBasePath);

					webzApp = new WebzEngine(dropboxFileSource, Arrays.asList(new FolderPreloadFilter(), new WelcomeFilter(),
							new FileFolderRedirectFilter(), ObsoleteWebzEngine.newFilter()));

				} catch (IOException | WebzException e) {
					throw new ServletException(e);
				}

				LOG.info("FINISHED INITIALIZING ROOT WEBZ APP");
			} else {
				LOG.info("ROOT WEBZ APP WAS ALREADY INITIALIZED - NO NEED TO INITIALIZE AGAIN");
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
