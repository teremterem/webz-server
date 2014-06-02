package org.terems.webz.servlet;

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
import org.terems.webz.dropbox.DropboxFileSystem;
import org.terems.webz.dropbox.gae.GaeHttpRequestor;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxRequestConfig;

@SuppressWarnings("serial")
public class WebzServlet extends HttpServlet {

	private static Logger LOG = LoggerFactory.getLogger(WebzServlet.class);

	private static final DbxRequestConfig DBX_CONFIG = new DbxRequestConfig("webz/0.1", Locale.getDefault().toString(),
			GaeHttpRequestor.INSTANCE/* for google app engine */);

	private WebzEngine webzEngine;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		String dropboxPath = config.getInitParameter("dropboxPath");
		WebzFileSystem dropboxFileSource = new DropboxFileSystem(new DbxClient(DBX_CONFIG,
				config.getInitParameter("accessToken")), dropboxPath);

		try {
			webzEngine = new WebzEngine(dropboxFileSource);
		} catch (WebzException e) {
			throw new ServletException(e.getMessage(), e);
		}

		LOG.info("Dropbox Path: " + dropboxPath);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
		webzEngine.fulfilRequest(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
		webzEngine.fulfilRequest(req, resp);
	}

}
