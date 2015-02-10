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

package org.terems.webz.internals.base;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzMetadata;
import org.terems.webz.WebzProperties;
import org.terems.webz.base.BaseWebzPropertiesInitable;
import org.terems.webz.internals.FreshParentChildrenMetadata;
import org.terems.webz.internals.ParentChildrenMetadata;
import org.terems.webz.internals.WebzFileSystemCache;
import org.terems.webz.internals.WebzFileSystemImpl;
import org.terems.webz.internals.WebzObjectFactory;
import org.terems.webz.internals.WebzPathNormalizer;

/**
 * Basic implementation of {@code WebzFileSystemImpl} to be extended by concrete implementations...
 **/
public abstract class BaseWebzFileSystemImpl extends BaseWebzPropertiesInitable implements WebzFileSystemImpl {

	private WebzPathNormalizer pathNormalizer;

	@Override
	public void init(WebzPathNormalizer pathNormalizer, WebzProperties properties, WebzObjectFactory factory) throws WebzException {

		this.pathNormalizer = pathNormalizer;
		init(properties);
	}

	protected WebzPathNormalizer getPathNormalizer() {
		return pathNormalizer;
	}

	/** Do nothing by default... **/
	@Override
	public void inflate(WebzFile file) throws IOException, WebzException {
	}

	/** Do nothing by default... **/
	@Override
	public void inflate(WebzFileSystemCache fileSystemCache, WebzFile file) throws IOException, WebzException {
	}

	/** Default implementation... **/
	@Override
	public FreshParentChildrenMetadata getParentChildrenMetadataIfChanged(String parentPathname, Object previousFolderHash)
			throws IOException, WebzException {

		ParentChildrenMetadata parentChildrenMetadata = getParentChildrenMetadata(parentPathname);
		if (previousFolderHash != null && parentChildrenMetadata != null && previousFolderHash.equals(parentChildrenMetadata.folderHash)) {
			return null;
		}

		return new FreshParentChildrenMetadata(parentChildrenMetadata);
	}

	/** Default implementation... **/
	@Override
	public Map<String, WebzMetadata> getChildPathnamesAndMetadata(String parentPathname) throws IOException, WebzException {
		ParentChildrenMetadata parentChildrenMetadata = getParentChildrenMetadata(parentPathname);
		return parentChildrenMetadata == null ? null : parentChildrenMetadata.childPathnamesAndMetadata;
	}

	/** Default implementation... **/
	@Override
	public Collection<String> getChildPathnames(String parentPathname) throws IOException, WebzException {

		ParentChildrenMetadata parentChildrenMetadata = getParentChildrenMetadata(parentPathname);
		if (parentChildrenMetadata == null || parentChildrenMetadata.childPathnamesAndMetadata == null) {
			return null;
		}

		return parentChildrenMetadata.childPathnamesAndMetadata.keySet();
	}

}
