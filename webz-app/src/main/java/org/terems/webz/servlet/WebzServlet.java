package org.terems.webz.servlet;

import java.util.Locale;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.WebzEngine;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFileSystem;
import org.terems.webz.impl.WebzEngineMain;
import org.terems.webz.impl.dropbox.DropboxFileSystem;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxRequestConfig;

@WebServlet(urlPatterns = "/*", initParams = { @WebInitParam(name = "dropboxPath", value = "/terems.org/webz/"),
		@WebInitParam(name = "accessToken", value = "QHNtgtFLQ8cAAAAAAAAAAUil0YtewX0DYd6LLu4vt5hbATbaiWkjLrbqBwbdfbl5") })
@SuppressWarnings("serial")
public class WebzServlet extends HttpServlet {

	private static final DbxRequestConfig DBX_CONFIG = new DbxRequestConfig("webz/0.1", Locale.getDefault().toString());
	// , GaeHttpRequestor.INSTANCE /* for google app engine */ );

	private WebzEngine webzEngine;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		try {
			WebzFileSystem dropboxFileSource = new DropboxFileSystem(new DbxClient(DBX_CONFIG,
					config.getInitParameter("accessToken")), config.getInitParameter("dropboxPath"));

			webzEngine = new WebzEngineMain(dropboxFileSource);
		} catch (WebzException e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) {
		// TODO leverage from "last modified" http logic supported in parent implementation ?
		webzEngine.fulfilRequest(req, resp);
	}

}
