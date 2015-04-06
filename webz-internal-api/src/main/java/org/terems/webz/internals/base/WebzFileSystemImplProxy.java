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

package org.terems.webz.internals.base;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzInputStreamDownloader;
import org.terems.webz.WebzMetadata;
import org.terems.webz.WebzProperties;
import org.terems.webz.base.BaseWebzDestroyable;
import org.terems.webz.internals.FreshParentChildrenMetadata;
import org.terems.webz.internals.ParentChildrenMetadata;
import org.terems.webz.internals.WebzFileSystemCache;
import org.terems.webz.internals.WebzFileSystemImpl;
import org.terems.webz.internals.WebzObjectFactory;
import org.terems.webz.internals.WebzPathNormalizer;

public abstract class WebzFileSystemImplProxy extends BaseWebzDestroyable implements WebzFileSystemImpl {

	protected abstract WebzFileSystemImpl getInternalImpl();

	@Override
	public void init(WebzPathNormalizer pathNormalizer, WebzProperties properties, WebzObjectFactory factory) throws WebzException {
		getInternalImpl().init(pathNormalizer, properties, factory);
	}

	@Override
	public String getUniqueId() {
		return getInternalImpl().getUniqueId();
	}

	@Override
	public void inflate(WebzFile file) throws IOException, WebzException {
		getInternalImpl().inflate(file);
	}

	@Override
	public void inflate(WebzFileSystemCache fsCache, WebzFile file) throws IOException, WebzException {
		getInternalImpl().inflate(fsCache, file);
	}

	@Override
	public WebzMetadata getMetadata(String pathname) throws IOException, WebzException {
		return getInternalImpl().getMetadata(pathname);
	}

	@Override
	public ParentChildrenMetadata getParentChildrenMetadata(String parentPathname) throws IOException, WebzException {
		return getInternalImpl().getParentChildrenMetadata(parentPathname);
	}

	@Override
	public FreshParentChildrenMetadata getParentChildrenMetadataIfChanged(String parentPathname, Object previousFolderHash)
			throws IOException, WebzException {
		return getInternalImpl().getParentChildrenMetadataIfChanged(parentPathname, previousFolderHash);
	}

	@Override
	public Map<String, WebzMetadata> getChildPathnamesAndMetadata(String parentPathname) throws IOException, WebzException {
		return getInternalImpl().getChildPathnamesAndMetadata(parentPathname);
	}

	@Override
	public Set<String> getChildPathnames(String parentPathname) throws IOException, WebzException {
		return getInternalImpl().getChildPathnames(parentPathname);
	}

	@Override
	public WebzInputStreamDownloader getFileDownloader(String pathname) throws IOException, WebzException {
		return getInternalImpl().getFileDownloader(pathname);
	}

}
