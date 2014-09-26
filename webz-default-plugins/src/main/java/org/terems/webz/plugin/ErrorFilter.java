package org.terems.webz.plugin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzException;
import org.terems.webz.config.StatusCodesConfig;
import org.terems.webz.plugin.base.BaseWebzFilter;
import org.terems.webz.util.WebzUtils;

public class ErrorFilter extends BaseWebzFilter {

	private static final Logger LOG = LoggerFactory.getLogger(ErrorFilter.class);

	private static final String FAILED_TO_SHOW_ERROR_MSG = "FAILED TO SHOW PROPER ERROR PAGE TO THE CLIENT";
	private static final String RESPONSE_ALREADY_COMMITTED_MSG = FAILED_TO_SHOW_ERROR_MSG + ": response is already committed";

	// TODO make RETHROW_IF_CANNOT_HANDLE configurable ?
	private static final boolean RETHROW_IF_CANNOT_HANDLE = false;

	private String pathTo500file;
	private StaticContentSender contentSender;

	@Override
	public void init() throws WebzException {
		pathTo500file = getAppConfig().getAppConfigObject(StatusCodesConfig.class).getPathTo500file();
		contentSender = new StaticContentSender(getAppConfig());
	}

	@Override
	public void serve(HttpServletRequest req, HttpServletResponse resp, WebzChainContext chainContext) throws IOException, WebzException {

		try {
			chainContext.nextPlease(req, resp);

		} catch (Throwable th) {

			LOG.warn(req.getMethod() + " " + WebzUtils.getFullUrl(req), th);

			if (resp.isCommitted()) {

				LOG.warn(RESPONSE_ALREADY_COMMITTED_MSG);
				if (RETHROW_IF_CANNOT_HANDLE) {
					throw th;
				}
			} else {
				try {
					resp.reset();
					resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

					contentSender.serveStaticContent(resp, chainContext.getFile(pathTo500file));

				} catch (Throwable th2) {

					LOG.warn(FAILED_TO_SHOW_ERROR_MSG, th2);
					if (RETHROW_IF_CANNOT_HANDLE) {
						throw th;
					}
				}
			}
		}
	}

}
