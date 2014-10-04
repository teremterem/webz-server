package org.terems.webz.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFilter;
import org.terems.webz.internals.WebzApp;
import org.terems.webz.internals.WebzDestroyableFactory;
import org.terems.webz.internals.WebzFileSystem;
import org.terems.webz.internals.WebzServletContainerBridge;
import org.terems.webz.util.WebzUtils;

public class WebzEngine implements WebzServletContainerBridge {

	private static final Logger LOG = LoggerFactory.getLogger(WebzEngine.class);

	private WebzDestroyableFactory globalFactory = new GenericWebzDestroyableFactory();
	private volatile WebzApp rootWebzApp;

	public WebzEngine(Properties rootFileSystemProperties, Collection<Class<? extends WebzFilter>> filterClassesList) throws WebzException {

		WebzFileSystem rootFileSystem = WebzFileSystemManager.getManager(globalFactory).createFileSystem(rootFileSystemProperties);

		rootWebzApp = globalFactory.newDestroyable(GenericWebzApp.class);
		rootWebzApp.init(rootFileSystem, filterClassesList, globalFactory.newDestroyable(GenericWebzDestroyableFactory.class));

		LOG.info("WebZ Engine started\n");
	}

	@Override
	public void serve(HttpServletRequest req, HttpServletResponse resp) throws IOException, WebzException {

		if (rootWebzApp == null) {
			throw new WebzException("WebZ Engine is already stopped");
		}

		if (LOG.isTraceEnabled()) {
			String modifiedSince = req.getHeader(WebzFilter.HEADER_IF_MODIFIED_SINCE);
			LOG.trace("\n\n\n\n// ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\\n "
					+ WebzUtils.formatRequestMethodAndUrl(req)
					+ "\n\\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ //"
					+ (modifiedSince == null ? "" : "\n " + WebzFilter.HEADER_IF_MODIFIED_SINCE + ": " + modifiedSince
							+ "\n// ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\") + "\n");
		}

		rootWebzApp.serve(req, resp);

		if (LOG.isTraceEnabled()) {
			String lastModified = resp.getHeader(WebzFilter.HEADER_LAST_MODIFIED);
			LOG.trace("\n\n\\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ //\n HTTP "
					+ resp.getStatus()
					+ " ("
					+ WebzUtils.formatRequestMethodAndUrl(req)
					+ ")\n// ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\"
					+ (lastModified == null ? "" : "\n " + WebzFilter.HEADER_LAST_MODIFIED + ": " + lastModified
							+ "\n\\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ //")
					+ "\n\n\n");
		}
	}

	@Override
	public void destroy() {

		rootWebzApp = null;
		LOG.info("WebZ Engine stopped\n");

		globalFactory.destroy();
		LOG.info("WebZ Engine destroyed\n");
	}

}
