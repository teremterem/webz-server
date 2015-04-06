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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.terems.webz.WebzException;
import org.terems.webz.WebzInputStreamDownloader;
import org.terems.webz.WebzMetadata.FileSpecific;
import org.terems.webz.WebzReadException;
import org.terems.webz.WebzReaderDownloader;
import org.terems.webz.WebzWriteException;
import org.terems.webz.util.WebzUtils;

public class FileDownloaderWithBOM implements WebzReaderDownloader {

	public static final ByteOrderMark[] ALL_BOMS = { ByteOrderMark.UTF_8, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_16LE,
			ByteOrderMark.UTF_32BE, ByteOrderMark.UTF_32LE };

	private String actualEncoding;
	private long actualNumberOfBytes;
	private BOMInputStream bomIn;
	private WebzInputStreamDownloader downloader;
	private Reader reader;

	@Override
	public String getActualEncoding() {
		return actualEncoding;
	}

	@Override
	public long getActualNumberOfBytes() {
		return actualNumberOfBytes;
	}

	@Override
	public long copyContentAndClose(OutputStream out) throws WebzReadException, WebzWriteException, IOException {
		return downloader.copyContentAndClose(out);
	}

	@Override
	public long copyContentAndClose(Writer writer) throws WebzReadException, WebzWriteException, IOException {

		try {
			return WebzUtils.copyReaderToWriter(reader, writer);
		} finally {
			close();
		}
	}

	@Override
	public String getContentAsStringAndClose() throws WebzReadException, WebzWriteException, IOException {

		if (actualNumberOfBytes > Integer.MAX_VALUE) {
			throw new IndexOutOfBoundsException("file is too big to be read as String (" + actualNumberOfBytes + " bytes)");
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream((int) actualNumberOfBytes);
		copyContentAndClose(out);
		return out.toString(actualEncoding);
	}

	public FileDownloaderWithBOM(WebzInputStreamDownloader downloader, String defaultEncoding) throws IOException, WebzException {

		this.bomIn = (BOMInputStream) new BOMInputStream(downloader.getInputStream(), false, ALL_BOMS);
		this.downloader = new FileDownloader(downloader.getFileSpecific(), bomIn);
		ByteOrderMark bom = bomIn.getBOM();

		if (bom == null) {
			actualEncoding = defaultEncoding;
			actualNumberOfBytes = downloader.getFileSpecific().getNumberOfBytes();
		} else {
			actualEncoding = bom.getCharsetName();
			actualNumberOfBytes = downloader.getFileSpecific().getNumberOfBytes() - bom.length();
		}
		reader = new InputStreamReader(bomIn, actualEncoding);
	}

	@Override
	public Reader getReader() throws IOException {
		return reader;
	}

	@Override
	public void close() {

		downloader.close();
		WebzUtils.closeSafely(reader);
		WebzUtils.closeSafely(bomIn);
	}

	@Override
	public FileSpecific getFileSpecific() {
		return downloader.getFileSpecific();
	}

}
