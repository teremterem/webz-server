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

package org.terems.webz;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

/** TODO !!! describe !!! **/
public interface WebzFile {

	/** TODO !!! describe !!! **/
	public String getPathname();

	/** TODO !!! describe !!! **/
	public boolean isPathnameInvalid();

	/** TODO !!! describe !!! **/
	public boolean isHidden() throws WebzPathnameException;

	/** TODO !!! describe !!! **/
	public WebzFile getParent() throws WebzPathnameException;

	/** TODO !!! describe !!! **/
	public WebzFile getDescendant(String relativePathname) throws WebzPathnameException;

	/** TODO !!! describe !!! **/
	public boolean belongsToSubtree(WebzFile subtree) throws WebzPathnameException;

	/** TODO !!! describe !!! **/
	public boolean belongsToSubtree(String subtreePath) throws WebzPathnameException;

	/** TODO !!! describe !!! **/
	public void inflate() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata getMetadata() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzFileDownloader getFileDownloader() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata.FileSpecific copyContentToOutputStream(OutputStream out) throws IOException, WebzReadException, WebzWriteException,
			WebzException;

	/** TODO !!! describe !!! **/
	public byte[] getFileContent() throws IOException, WebzReadException, WebzWriteException, WebzException;

	/** TODO !!! describe !!! **/
	public Collection<WebzFile> listChildren() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata createFolder() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata uploadFile(InputStream content, long numBytes) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata uploadFile(InputStream content) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata uploadFile(byte[] content) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata move(WebzFile destFile) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata copy(WebzFile destFile) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata move(String destPathname) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata copy(String destPathname) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public void delete() throws IOException, WebzException;

}
