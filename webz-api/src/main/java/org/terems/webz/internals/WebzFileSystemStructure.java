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

package org.terems.webz.internals;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzMetadata;

public interface WebzFileSystemStructure {

	public void inflate(WebzFile file) throws IOException, WebzException;

	public void inflate(WebzFileSystemCache fsCache, WebzFile file) throws IOException, WebzException;

	public WebzMetadata getMetadata(String pathname) throws IOException, WebzException;

	public ParentChildrenMetadata getParentChildrenMetadata(String parentPathname) throws IOException, WebzException;

	/**
	 * @return {@code null} if folder hash has not changed, otherwise - {@code FreshParentChildrenMetadata} object that encapsulates
	 *         {@code ParentChildrenMetadata} (<b>NOTE:</b> encapsulated {@code ParentChildrenMetadata} may be {@code null} if there is no
	 *         such file or folder)
	 **/
	public FreshParentChildrenMetadata getParentChildrenMetadataIfChanged(String parentPathname, Object previousFolderHash)
			throws IOException, WebzException;

	public Map<String, WebzMetadata> getChildPathnamesAndMetadata(String parentPathname) throws IOException, WebzException;

	public Collection<String> getChildPathnames(String parentPathname) throws IOException, WebzException;

}
