/*
 * WebZ Server is a server that can serve web pages from various sources.
 * Copyright (C) 2013-2015  Oleksandr Tereschenko <http://www.terems.org/>
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

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzConfig;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzMetadata;
import org.terems.webz.WebzWriteException;
import org.terems.webz.config.GeneralAppConfig;
import org.terems.webz.config.MimetypesConfig;
import org.terems.webz.util.WebzUtils;

public class StaticContentSender {

	public static final ByteOrderMark[] ALL_BOMS = { ByteOrderMark.UTF_8, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_16LE,
			ByteOrderMark.UTF_32BE, ByteOrderMark.UTF_32LE };

	private static final Logger LOG = LoggerFactory.getLogger(StaticContentSender.class);

	private MimetypesConfig mimetypes;
	private String defaultMimetype;
	private String defaultEncoding;

	public StaticContentSender(WebzConfig config) throws WebzException {

		mimetypes = config.getAppConfigObject(MimetypesConfig.class);

		GeneralAppConfig appConfig = config.getAppConfigObject(GeneralAppConfig.class);
		defaultMimetype = appConfig.getDefaultMimetype();
		defaultEncoding = appConfig.getDefaultEncoding();
	}

	public WebzMetadata.FileSpecific serveStaticContent(HttpServletRequest req, HttpServletResponse resp, WebzFile content)
			throws IOException, WebzException {

		WebzFileDownloader downloader = content.getFileDownloader();
		if (downloader == null) {
			return null;
		}
		WebzMetadata.FileSpecific fileSpecific = downloader.fileSpecific;
		if (fileSpecific == null) {
			return null;
		}

		BOMInputStream bomIn = new BOMInputStream(downloader.content, false, ALL_BOMS);

		String encoding = defaultEncoding;
		long contentLength = fileSpecific.getNumberOfBytes();

		ByteOrderMark bom = bomIn.getBOM();
		if (bom != null) {
			encoding = bom.getCharsetName();
			contentLength -= bom.length();
		}
		resp.setContentType(mimetypes.getMimetype(fileSpecific, defaultMimetype));
		resp.setCharacterEncoding(encoding);

		resp.setContentLength((int) contentLength);
		resp.addHeader("Content-Length", Long.toString(contentLength));
		// with Servlet API 3.1 it could be: resp.setContentLengthLong(contentLength);

		try {
			if (!WebzUtils.isHttpMethodHead(req)) {
				WebzUtils.copyInToOut(bomIn, resp.getOutputStream());
			}

		} catch (WebzWriteException e) {

			if (LOG.isDebugEnabled()) {
				LOG.debug("most likely client dropped connection while receiving static content from " + content, e);
			}
		} finally {
			WebzUtils.closeSafely(bomIn);
		}
		resp.flushBuffer();

		return fileSpecific;
	}
}
