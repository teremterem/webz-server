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

package org.terems.webz;

import java.io.InputStream;
import java.io.OutputStream;

import org.terems.webz.util.WebzUtils;

/** TODO !!! describe !!! **/
public class WebzFileDownloader {

	/** TODO !!! describe !!! **/
	public final WebzMetadata.FileSpecific fileSpecific;

	/** TODO !!! describe !!! **/
	public final InputStream content;

	/** TODO !!! describe !!! **/
	public long copyContentAndClose(OutputStream out) throws WebzReadException, WebzWriteException {

		try {
			return WebzUtils.copyInToOut(content, out);
		} finally {
			close();
		}
	}

	/** TODO !!! describe !!! **/
	public void close() {
		WebzUtils.closeSafely(content);
	}

	/** TODO !!! describe !!! **/
	public WebzFileDownloader(WebzMetadata.FileSpecific fileSpecific, InputStream content) {
		this.fileSpecific = fileSpecific;
		this.content = content;
	}

}
