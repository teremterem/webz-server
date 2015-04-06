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

package org.terems.webz.internals;

import java.io.InputStream;
import java.io.OutputStream;

import org.terems.webz.WebzInputStreamDownloader;
import org.terems.webz.WebzMetadata;
import org.terems.webz.WebzReadException;
import org.terems.webz.WebzWriteException;
import org.terems.webz.util.WebzUtils;

public class FileDownloader implements WebzInputStreamDownloader {

	private WebzMetadata.FileSpecific fileSpecific;

	private InputStream in;

	@Override
	public WebzMetadata.FileSpecific getFileSpecific() {
		return fileSpecific;
	}

	@Override
	public InputStream getInputStream() {
		return in;
	}

	@Override
	public long copyContentAndClose(OutputStream out) throws WebzReadException, WebzWriteException {

		try {
			return WebzUtils.copyInToOut(in, out);
		} finally {
			close();
		}
	}

	@Override
	public void close() {
		WebzUtils.closeSafely(in);
	}

	public FileDownloader(WebzMetadata.FileSpecific fileSpecific, InputStream inputStream) {
		this.fileSpecific = fileSpecific;
		this.in = inputStream;
	}

}
