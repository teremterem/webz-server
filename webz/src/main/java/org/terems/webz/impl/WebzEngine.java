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
import org.terems.webz.WebzProperties;
import org.terems.webz.git.impl.WebzGit;
import org.terems.webz.internals.WebzApp;
import org.terems.webz.internals.WebzDestroyableObjectFactory;
import org.terems.webz.internals.WebzFileSystem;
import org.terems.webz.internals.WebzServletContainerBridge;
import org.terems.webz.util.WebzUtils;

public class WebzEngine implements WebzServletContainerBridge {

	private static final Logger LOG = LoggerFactory.getLogger(WebzEngine.class);

	private WebzDestroyableObjectFactory globalFactory = new GenericWebzObjectFactory();
	private volatile WebzApp rootWebzApp;
	private WebzGit git;

	public WebzEngine(Properties rootFileSystemProperties, Collection<Class<? extends WebzFilter>> filterClassesList) {

		try {
			String gitOriginUrl = rootFileSystemProperties.getProperty(WebzProperties.GIT_ORIGIN_URL_PROPERTY);
			if (gitOriginUrl != null) {
				git = new WebzGit(gitOriginUrl, rootFileSystemProperties.getProperty(WebzProperties.FS_BASE_PATH_PROPERTY));
				// TODO STORAGE_PATH = System.getEnv("OPENSHIFT_DATA_DIR") == null ? "/home/shekhar/tmp/" : openshiftDataDir;
			}

			WebzFileSystem rootFileSystem = WebzFileSystemManager.getManager(globalFactory).createFileSystem(rootFileSystemProperties);

			rootWebzApp = globalFactory.newDestroyable(GenericWebzApp.class).init(rootFileSystem, filterClassesList,
					globalFactory.newDestroyable(GenericWebzObjectFactory.class));

		} catch (WebzException e) {

			if (LOG.isErrorEnabled()) {
				LOG.error("failed to init WebZ App '" + (rootWebzApp == null ? null : rootWebzApp.getDisplayName()) + "': " + e.toString(),
						e);
			}
		}

		LOG.info("WebZ Engine started\n\n\n");
	}

	@Override
	public void serve(HttpServletRequest req, HttpServletResponse resp) throws IOException, WebzException {

		if (git != null && "/pull/from/origin".equals(req.getPathInfo())) {

			git.pull();
			resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
			resp.setHeader(WebzFilter.HEADER_LOCATION, "/");

			return;
		}

		if (rootWebzApp == null) {
			throw new WebzException("root WebZ App is already stopped or has not been started yet");
		}

		traceRequestStart(req);

		rootWebzApp.serve(req, resp);

		traceRequestEnd(req, resp);
	}

	@Override
	public void destroy() {

		rootWebzApp = null;
		LOG.info("WebZ Engine stopped\n\n\n");

		globalFactory.destroy();
		LOG.info("WebZ Engine destroyed\n\n\n");

		if (git != null) {
			git.destroy();
		}
	}

	private void traceRequestStart(HttpServletRequest req) {
		if (LOG.isTraceEnabled()) {
			String modifiedSince = req.getHeader(WebzFilter.HEADER_IF_MODIFIED_SINCE);
			LOG.trace("\n\n\n\n// ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\\n "
					+ WebzUtils.formatRequestMethodAndUrl(req)
					+ "\n\\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ //"
					+ (modifiedSince == null ? "" : "\n " + WebzFilter.HEADER_IF_MODIFIED_SINCE + ": " + modifiedSince
							+ "\n// ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\") + "\n");
		}
	}

	private void traceRequestEnd(HttpServletRequest req, HttpServletResponse resp) {
		if (LOG.isTraceEnabled()) {
			String lastModified = resp.getHeader(WebzFilter.HEADER_LAST_MODIFIED);
			String contentLength = resp.getHeader(WebzFilter.HEADER_CONTENT_LENGTH);
			String contentType = resp.getHeader(WebzFilter.HEADER_CONTENT_TYPE);
			LOG.trace("\n\n\\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ //\n HTTP "
					+ resp.getStatus()
					+ " ("
					+ WebzUtils.formatRequestMethodAndUrl(req)
					+ ")"
					+ (contentLength == null ? "" : " - " + contentLength + " bytes")
					+ "\n// ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\ // ~~~ \\\\"
					+ (lastModified == null ? "" : "\n " + WebzFilter.HEADER_LAST_MODIFIED + ": " + lastModified)
					+ (contentType == null ? "" : "\n " + WebzFilter.HEADER_CONTENT_TYPE + ": " + contentType)
					+ (lastModified == null && contentType == null ? ""
							: "\n\\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ // \\\\ ~~~ //")
					+ "\n\n\n");
		}
	}

}
