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

package org.terems.webz.impl;

import java.io.File;
import java.io.IOException;

import org.terems.webz.WebzException;
import org.terems.webz.WebzMetadata;
import org.terems.webz.base.BaseWebzMetadata;

public class LocalFileMetadata extends BaseWebzMetadata implements WebzMetadata {

	private File file;

	public LocalFileMetadata(File file) {
		this.file = file;
	}

	@Override
	public long getNumberOfBytes() throws IOException, WebzException {
		return file.length();
	}

	@Override
	public Long getLastModified() throws IOException, WebzException {
		return file.lastModified();
	}

	@Override
	public String getRevision() throws IOException, WebzException {
		return String.valueOf(getLastModified());
	}

	@Override
	public String getName() throws IOException, WebzException {
		return file.getName();
	}

	@Override
	public boolean isFile() throws IOException, WebzException {
		return file.isFile();
	}

	@Override
	public boolean isFolder() throws IOException, WebzException {
		return file.isDirectory();
	}

}
