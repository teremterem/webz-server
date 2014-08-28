package org.terems.webz.filter;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzException;
import org.terems.webz.plugin.base.BaseWebzFilter;

public class ErrorFilter extends BaseWebzFilter {

	private static final Logger LOG = LoggerFactory.getLogger(ErrorFilter.class);

	// TODO move it to config folder
	private String pathTo500html = "500.html";
	private boolean rethrowIfCannotHandle = false;

	@Override
	public void serve(HttpServletRequest req, HttpServletResponse resp, WebzChainContext chainContext) throws IOException, WebzException {

		try {
			chainContext.nextPlease(req, resp);

		} catch (Throwable th) {

			LOG.error(th.getMessage(), th);

			if (resp.isCommitted()) {

				if (rethrowIfCannotHandle) {
					throw th;
				}
			} else {
				try {
					resp.reset();
					resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					chainContext.getFile(pathTo500html).fileContentToOutputStream(resp.getOutputStream());

				} catch (Throwable th2) {

					LOG.error("FAILED TO DISPLAY AN ERROR PAGE: " + th2.getMessage(), th2);

					if (rethrowIfCannotHandle) {
						throw th;
					}
				}
			}
		}
	}

}
