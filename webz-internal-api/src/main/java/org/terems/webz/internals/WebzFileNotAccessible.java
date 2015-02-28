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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzMetadata;
import org.terems.webz.WebzMetadata.FileSpecific;
import org.terems.webz.WebzPathnameException;
import org.terems.webz.WebzReadException;
import org.terems.webz.WebzWriteException;

public class WebzFileNotAccessible implements WebzFile {

	private WebzFile file;
	private Throwable cause = null;

	public WebzFileNotAccessible(WebzFile file) {
		this.file = file;
	}

	public WebzFileNotAccessible(WebzFile file, Throwable cause) {
		this(file);
		this.cause = cause;
	}

	@Override
	public String getPathname() {
		return file.getPathname();
	}

	@Override
	public boolean isPathnameInvalid() {
		return file.isPathnameInvalid();
	}

	@Override
	public boolean isHidden() throws WebzPathnameException {
		return file.isHidden();
	}

	@Override
	public WebzFile getParent() throws WebzPathnameException {
		return file.getParent();
	}

	@Override
	public WebzFile getDescendant(String relativePathname) throws WebzPathnameException {
		return file.getDescendant(relativePathname);
	}

	@Override
	public boolean belongsToSubtree(WebzFile subtree) throws WebzPathnameException {
		return file.belongsToSubtree(subtree);
	}

	@Override
	public boolean belongsToSubtree(String subtreePath) throws WebzPathnameException {
		return file.belongsToSubtree(subtreePath);
	}

	@Override
	public void inflate() throws IOException, WebzException {
	}

	@Override
	public WebzMetadata getMetadata() throws IOException, WebzException {
		return null;
	}

	@Override
	public WebzFileDownloader getFileDownloader() throws IOException, WebzException {
		return null;
	}

	@Override
	public FileSpecific copyContentToOutputStream(OutputStream out) throws IOException, WebzReadException, WebzWriteException,
			WebzException {
		return null;
	}

	@Override
	public byte[] getFileContent() throws IOException, WebzException {
		return null;
	}

	@Override
	public Collection<WebzFile> listChildren() throws IOException, WebzException {
		return null;
	}

	@Override
	public WebzMetadata createFolder() throws IOException, WebzException {
		throw accessDenied();
	}

	@Override
	public WebzMetadata uploadFile(InputStream content, long numBytes) throws IOException, WebzException {
		throw accessDenied();
	}

	@Override
	public WebzMetadata uploadFile(InputStream content) throws IOException, WebzException {
		throw accessDenied();
	}

	@Override
	public WebzMetadata uploadFile(byte[] content) throws IOException, WebzException {
		throw accessDenied();
	}

	@Override
	public WebzMetadata move(WebzFile destFile) throws IOException, WebzException {
		throw accessDenied();
	}

	@Override
	public WebzMetadata copy(WebzFile destFile) throws IOException, WebzException {
		throw accessDenied();
	}

	@Override
	public WebzMetadata move(String destPathname) throws IOException, WebzException {
		throw accessDenied();
	}

	@Override
	public WebzMetadata copy(String destPathname) throws IOException, WebzException {
		throw accessDenied();
	}

	@Override
	public void delete() throws IOException, WebzException {
		throw accessDenied();
	}

	private WebzException accessDenied() {

		if (cause == null) {
			return new NotAccessibleWebzException("access denied");
		} else {
			return new NotAccessibleWebzException(cause);
		}
	}

}
