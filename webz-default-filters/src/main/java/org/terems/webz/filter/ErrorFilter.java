/*
 * WebZ Server is a server that can serve web pages from various sources.
 * Copyright (C) 2013-2015  Oleksandr Tereschenko <http://ww.webz.bz/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.terems.webz.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzMetadata;
import org.terems.webz.base.BaseWebzFilter;
import org.terems.webz.config.StatusCodesConfig;
import org.terems.webz.util.WebzUtils;

public class ErrorFilter extends BaseWebzFilter {

	private static final Logger LOG = LoggerFactory.getLogger(ErrorFilter.class);

	private static final String FAILED_TO_SHOW_ERROR_MSG = "FAILED TO SHOW PROPER ERROR PAGE TO THE CLIENT";
	private static final String RESPONSE_ALREADY_COMMITTED_MSG = FAILED_TO_SHOW_ERROR_MSG + ": response is already committed";

	// TODO make rethrowIfCannotHandle configurable ?
	private final boolean rethrowIfCannotHandle = false;

	private String pathTo500file;
	private StaticContentSender contentSender;

	@Override
	public void init() throws WebzException {
		pathTo500file = getAppConfig().getAppConfigObject(StatusCodesConfig.class).getPathTo500file();
		contentSender = new StaticContentSender(getAppConfig());
	}

	@Override
	public void serve(HttpServletRequest req, HttpServletResponse resp, WebzChainContext chainContext) throws WebzException {

		try {
			chainContext.nextPlease(req, resp);

		} catch (Throwable th) {

			if (LOG.isErrorEnabled()) {
				LOG.error(WebzUtils.formatRequestMethodAndUrl(req), th);
			}

			if (resp.isCommitted()) {

				LOG.warn(RESPONSE_ALREADY_COMMITTED_MSG);
				if (rethrowIfCannotHandle) {
					throw new WebzException(th);
				}
			} else {

				WebzFile errorFile = null;
				WebzMetadata.FileSpecific errorFileMetadata = null;
				Throwable exceptionWhileShowingErrorPage = null;

				resp.reset();
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

				if (pathTo500file != null) {
					try {
						errorFile = chainContext.getFile(pathTo500file);
						errorFileMetadata = contentSender.serveStaticContent(req, resp, errorFile);

					} catch (Throwable th2) {
						exceptionWhileShowingErrorPage = th2;
					}

					if (exceptionWhileShowingErrorPage != null) {

						LOG.warn(FAILED_TO_SHOW_ERROR_MSG, exceptionWhileShowingErrorPage);
						if (rethrowIfCannotHandle) {
							throw new WebzException(th);
						}
					} else if (errorFileMetadata == null) {

						if (LOG.isWarnEnabled()) {
							LOG.warn(FAILED_TO_SHOW_ERROR_MSG + ": '" + (errorFile == null ? pathTo500file : errorFile.getPathname())
									+ "' does not exist");
						}
						if (rethrowIfCannotHandle) {
							throw new WebzException(th);
						}
					}
				}
			}
		}
	}

}
