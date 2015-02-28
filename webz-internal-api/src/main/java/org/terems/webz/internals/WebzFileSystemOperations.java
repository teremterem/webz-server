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

import org.terems.webz.WebzException;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzMetadata;

public interface WebzFileSystemOperations {

	public WebzFileDownloader getFileDownloader(String pathname) throws IOException, WebzException;

	public WebzMetadata createFolder(String pathname) throws IOException, WebzException;

	public WebzMetadata.FileSpecific uploadFile(String pathname, InputStream content, long numBytes) throws IOException, WebzException;

	public WebzMetadata.FileSpecific uploadFile(String pathname, InputStream content) throws IOException, WebzException;

	public WebzMetadata move(String srcPathname, String destPathname) throws IOException, WebzException;

	public WebzMetadata copy(String srcPathname, String destPathname) throws IOException, WebzException;

	public void delete(String pathname) throws IOException, WebzException;

}
