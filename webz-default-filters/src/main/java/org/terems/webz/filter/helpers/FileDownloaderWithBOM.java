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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzReadException;
import org.terems.webz.WebzWriteException;
import org.terems.webz.util.WebzUtils;

public class FileDownloaderWithBOM {

	public static final ByteOrderMark[] ALL_BOMS = { ByteOrderMark.UTF_8, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_16LE,
			ByteOrderMark.UTF_32BE, ByteOrderMark.UTF_32LE };

	public final BOMInputStream content;
	public final String actualEncoding;
	public final long actualNumberOfBytes;

	public long copyContentAndClose(OutputStream out) throws WebzReadException, WebzWriteException {

		try {
			return WebzUtils.copyInToOut(content, out);
		} finally {
			close();
		}
	}

	public void close() {
		WebzUtils.closeSafely(content);
	}

	public String getContentAsStringAndClose() throws WebzReadException, WebzWriteException, UnsupportedEncodingException {

		if (actualNumberOfBytes > Integer.MAX_VALUE) {
			throw new IndexOutOfBoundsException("file is too big to be read as String (" + actualNumberOfBytes + " bytes)");
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream((int) actualNumberOfBytes);
		copyContentAndClose(out);
		return out.toString(actualEncoding);
	}

	public FileDownloaderWithBOM(WebzFileDownloader downloader, String defaultEncoding) throws IOException, WebzException {

		content = new BOMInputStream(downloader.content, false, ALL_BOMS);
		ByteOrderMark bom = content.getBOM();

		if (bom == null) {
			actualEncoding = defaultEncoding;
			actualNumberOfBytes = downloader.fileSpecific.getNumberOfBytes();
		} else {
			actualEncoding = bom.getCharsetName();
			actualNumberOfBytes = downloader.fileSpecific.getNumberOfBytes() - bom.length();
		}
	}

}
