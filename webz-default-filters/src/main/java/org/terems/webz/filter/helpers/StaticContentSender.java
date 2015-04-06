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

package org.terems.webz.filter.helpers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzConfig;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzMetadata;
import org.terems.webz.WebzReaderDownloader;
import org.terems.webz.WebzWriteException;
import org.terems.webz.config.GeneralAppConfig;
import org.terems.webz.config.MimetypesConfig;
import org.terems.webz.util.WebzUtils;

public class StaticContentSender {

	private static final Logger LOG = LoggerFactory.getLogger(StaticContentSender.class);

	private MimetypesConfig mimetypes;
	private String defaultMimetype;

	public StaticContentSender(WebzConfig config) throws WebzException {

		mimetypes = config.getConfigObject(MimetypesConfig.class);

		GeneralAppConfig appConfig = config.getConfigObject(GeneralAppConfig.class);
		defaultMimetype = appConfig.getDefaultMimetype();
	}

	public WebzMetadata.FileSpecific serveStaticContent(HttpServletRequest req, HttpServletResponse resp, WebzFile content)
			throws IOException, WebzException {

		WebzReaderDownloader downloader = content.getFileDownloader();
		if (downloader == null) {
			return null;
		}
		WebzMetadata.FileSpecific fileSpecific = WebzUtils.assertNotNull(downloader.getFileSpecific());

		WebzUtils.prepareStandardHeaders(resp, mimetypes.getMimetype(fileSpecific, defaultMimetype), downloader.getActualEncoding(),
				downloader.getActualNumberOfBytes());
		try {
			if (!WebzUtils.isHttpMethodHead(req)) {
				downloader.copyContentAndClose(resp.getOutputStream());
			}

		} catch (WebzWriteException e) {
			debugConnectionDropped(e, content);
		}
		try {
			resp.flushBuffer();

		} catch (IOException e) {
			debugConnectionDropped(e, content);
		}

		return fileSpecific;
	}

	private void debugConnectionDropped(IOException e, WebzFile content) {

		if (LOG.isDebugEnabled()) {
			LOG.debug("most likely client dropped connection while receiving static content from " + content, e);
		}
	}
}
