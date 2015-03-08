/*
 * Copyright 2014-2015 Oleksandr Tereschenko <http://www.terems.org/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
