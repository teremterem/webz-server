package org.terems.webz.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFilter;
import org.terems.webz.impl.WebzEngine;
import org.terems.webz.plugin.ErrorFilter;
import org.terems.webz.plugin.NotFoundFilter;
import org.terems.webz.plugin.StaticContentFilter;
import org.terems.webz.plugin.WelcomeFilter;
import org.terems.webz.util.WebzUtils;

@SuppressWarnings("serial")
public class WebzHttpServletEnvelope extends HttpServlet {

	private static final String ROOT_FILE_SYSTEM_PROPERTIES_PARAM = "rootFileSystemProperties";

	@Override
	public void init() throws ServletException {
		try {
			// TODO decide if WebzEngine lazy initialization mechanism is needed at all
			getWebzEngine();

		} catch (IOException e) {
			throw new ServletException(e);
		} catch (WebzException e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		try {
			getWebzEngine().serve(req, resp);

		} catch (WebzException e) {
			throw new ServletException(e);
		}
	}

	/**
	 * <a href="http://en.wikipedia.org/wiki/Double-checked_locking#Usage_in_Java">Double-checked locking - Usage in Java</a>
	 **/
	private WebzEngine getWebzEngine() throws IOException, WebzException {

		WebzEngine webzEngine = this.webzEngine;
		if (webzEngine == null) {

			synchronized (webzEngineMutex) {

				webzEngine = this.webzEngine;
				if (webzEngine == null) {

					Properties rootFileSystemProperties = WebzUtils.loadPropertiesFromClasspath(
							getServletConfig().getInitParameter(ROOT_FILE_SYSTEM_PROPERTIES_PARAM), true);

					// // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ // ~~~ \\ //
					this.webzEngine = webzEngine = new WebzEngine(rootFileSystemProperties, getDefaultFilterClassesList());
					// \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\ ~~~ // \\
				}
			}
		}
		return webzEngine;
	}

	private volatile WebzEngine webzEngine;
	private final Object webzEngineMutex = new Object();

	@Override
	public void destroy() {

		synchronized (webzEngineMutex) {

			WebzEngine webzEngine = this.webzEngine;
			if (webzEngine != null) {

				this.webzEngine = null;
				webzEngine.destroy();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Collection<Class<? extends WebzFilter>> getDefaultFilterClassesList() {

		// TODO move list of filters into webz app config
		Class<?>[] filterClassesList = { ErrorFilter.class, WelcomeFilter.class, StaticContentFilter.class, NotFoundFilter.class };

		return Arrays.asList((Class<? extends WebzFilter>[]) filterClassesList);
	}

}
