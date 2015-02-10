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

package org.terems.webz.base;

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

/** TODO !!! describe !!! **/
public abstract class WebzFileProxy implements WebzFile {

	/** TODO !!! describe !!! **/
	protected abstract WebzFile getInnerFile();

	@Override
	public String getPathname() {
		return getInnerFile().getPathname();
	}

	@Override
	public boolean isPathnameInvalid() {
		return getInnerFile().isPathnameInvalid();
	}

	@Override
	public boolean isHidden() throws WebzPathnameException {
		return getInnerFile().isHidden();
	}

	@Override
	public WebzFile getParent() throws WebzPathnameException {
		return getInnerFile().getParent();
	}

	@Override
	public WebzFile getDescendant(String relativePathname) throws WebzPathnameException {
		return getInnerFile().getDescendant(relativePathname);
	}

	@Override
	public boolean belongsToSubtree(WebzFile subtree) throws WebzPathnameException {
		return getInnerFile().belongsToSubtree(subtree);
	}

	@Override
	public boolean belongsToSubtree(String subtreePath) throws WebzPathnameException {
		return getInnerFile().belongsToSubtree(subtreePath);
	}

	@Override
	public void inflate() throws IOException, WebzException {
		getInnerFile().inflate();
	}

	@Override
	public WebzMetadata getMetadata() throws IOException, WebzException {
		return getInnerFile().getMetadata();
	}

	@Override
	public WebzFileDownloader getFileDownloader() throws IOException, WebzException {
		return getInnerFile().getFileDownloader();
	}

	@Override
	public FileSpecific copyContentToOutputStream(OutputStream out) throws IOException, WebzReadException, WebzWriteException,
			WebzException {
		return getInnerFile().copyContentToOutputStream(out);
	}

	@Override
	public byte[] getFileContent() throws IOException, WebzReadException, WebzWriteException, WebzException {
		return getInnerFile().getFileContent();
	}

	@Override
	public Collection<WebzFile> listChildren() throws IOException, WebzException {
		return getInnerFile().listChildren();
	}

	@Override
	public WebzMetadata createFolder() throws IOException, WebzException {
		return getInnerFile().createFolder();
	}

	@Override
	public WebzMetadata uploadFile(InputStream content, long numBytes) throws IOException, WebzException {
		return getInnerFile().uploadFile(content, numBytes);
	}

	@Override
	public WebzMetadata uploadFile(InputStream content) throws IOException, WebzException {
		return getInnerFile().uploadFile(content);
	}

	@Override
	public WebzMetadata uploadFile(byte[] content) throws IOException, WebzException {
		return getInnerFile().uploadFile(content);
	}

	@Override
	public WebzMetadata move(WebzFile destFile) throws IOException, WebzException {
		return getInnerFile().move(destFile);
	}

	@Override
	public WebzMetadata copy(WebzFile destFile) throws IOException, WebzException {
		return getInnerFile().copy(destFile);
	}

	@Override
	public WebzMetadata move(String destPathname) throws IOException, WebzException {
		return getInnerFile().move(destPathname);
	}

	@Override
	public WebzMetadata copy(String destPathname) throws IOException, WebzException {
		return getInnerFile().copy(destPathname);
	}

	@Override
	public void delete() throws IOException, WebzException {
		getInnerFile().delete();
	}

}
