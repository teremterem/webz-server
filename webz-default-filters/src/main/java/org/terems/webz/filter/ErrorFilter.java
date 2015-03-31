/*
 * WebZ Server can serve web pages from various local and remote file sources.
 * Copyright (C) 2014-2015  Oleksandr Tereschenko <http://www.terems.org/>
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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzMetadata;
import org.terems.webz.base.BaseWebzFilter;
import org.terems.webz.config.StatusCodesConfig;
import org.terems.webz.filter.helpers.StaticContentSender;
import org.terems.webz.util.WebzUtils;

public class ErrorFilter extends BaseWebzFilter {

	private static final Logger LOG = LoggerFactory.getLogger(ErrorFilter.class);

	private static final String FAILED_TO_SHOW_ERROR_MSG = "FAILED TO SHOW PROPER ERROR PAGE TO THE CLIENT";
	private static final String RESPONSE_ALREADY_COMMITTED_MSG = FAILED_TO_SHOW_ERROR_MSG + ": response is already committed";

	private String pathTo500file;
	private StaticContentSender contentSender;

	@Override
	public void init(WebzContext context) throws IOException, WebzException {
		pathTo500file = getAppConfig().getConfigObject(StatusCodesConfig.class).getPathTo500file();
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
			} else {

				WebzFile errorFile = null;
				WebzMetadata.FileSpecific errorFileMetadata = null;
				Throwable exceptionWhileShowingErrorPage = null;

				resp.reset();
				resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

				// TODO come up with an alternative error page when pathTo500file is null
				if (pathTo500file != null) {
					try {
						errorFile = chainContext.getFile(pathTo500file);
						errorFileMetadata = contentSender.serveStaticContent(req, resp, errorFile);

					} catch (Throwable th2) {
						exceptionWhileShowingErrorPage = th2;
					}

					if (exceptionWhileShowingErrorPage != null) {

						LOG.warn(FAILED_TO_SHOW_ERROR_MSG, exceptionWhileShowingErrorPage);
					} else if (errorFileMetadata == null) {

						if (LOG.isWarnEnabled()) {
							LOG.warn(FAILED_TO_SHOW_ERROR_MSG + ": '" + (errorFile == null ? pathTo500file : errorFile.getPathname())
									+ "' does not exist");
						}
					}
				}
			}
		}
	}

}
