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

package org.terems.webz.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzMetadata;
import org.terems.webz.WebzMetadata.FileSpecific;
import org.terems.webz.internals.ParentChildrenMetadata;
import org.terems.webz.internals.WebzFileSystem;
import org.terems.webz.internals.base.BaseWebzFileSystemImpl;

public class SiteAndSpaFileSystemImpl extends BaseWebzFileSystemImpl {

	private static final Logger LOG = LoggerFactory.getLogger(SiteAndSpaFileSystemImpl.class);

	private WebzFileSystem siteFileSystem;
	private WebzFileSystem spaFileSystem;

	private String uniqueId;

	public SiteAndSpaFileSystemImpl(WebzFileSystem siteFileSystem, WebzFileSystem spaFileSystem) {

		this.siteFileSystem = siteFileSystem;
		this.spaFileSystem = spaFileSystem;
	}

	@Override
	protected void init() {

		uniqueId = siteFileSystem.getUniqueId() + "-" + spaFileSystem.getUniqueId();

		if (LOG.isInfoEnabled()) {
			LOG.info("'" + uniqueId + "' hybrid file system was created");
		}
	}

	@Override
	public String getUniqueId() {
		return uniqueId;
	}

	private WebzFile findFile(String pathname) {

		// TODO
		return null;
	}

	@Override
	public WebzMetadata getMetadata(String pathname) throws IOException, WebzException {
		return findFile(pathname).getMetadata();
	}

	@Override
	public ParentChildrenMetadata getParentChildrenMetadata(String parentPathname) throws IOException, WebzException {

		WebzFile file = findFile(parentPathname);

		WebzMetadata metadata = file.getMetadata();
		if (metadata == null) {
			return null;
		}

		ParentChildrenMetadata parentChildren = new ParentChildrenMetadata();
		parentChildren.parentMetadata = metadata;
		parentChildren.childPathnamesAndMetadata = populateChildPathnamesAndMetadata(file);

		return parentChildren;
	}

	@Override
	public Map<String, WebzMetadata> getChildPathnamesAndMetadata(String parentPathname) throws IOException, WebzException {
		return populateChildPathnamesAndMetadata(findFile(parentPathname));
	}

	private Map<String, WebzMetadata> populateChildPathnamesAndMetadata(WebzFile file) throws IOException, WebzException {

		Collection<WebzFile> children = file.listChildren();
		if (children == null) {
			return null;
		}

		Map<String, WebzMetadata> pathnamesAndMetadata = new LinkedHashMap<String, WebzMetadata>();
		for (WebzFile child : children) {
			pathnamesAndMetadata.put(child.getPathname(), child.getMetadata());
		}
		return pathnamesAndMetadata;
	}

	@Override
	public Collection<String> getChildPathnames(String parentPathname) throws IOException, WebzException {

		WebzFile file = findFile(parentPathname);
		Collection<WebzFile> children = file.listChildren();
		if (children == null) {
			return null;
		}

		Collection<String> childPathnames = new ArrayList<String>(children.size());
		for (WebzFile child : children) {
			childPathnames.add(child.getPathname());
		}
		return childPathnames;
	}

	@Override
	public WebzFileDownloader getFileDownloader(String pathname) throws IOException, WebzException {

		return findFile(pathname).getFileDownloader();
	}

	@Override
	public WebzMetadata createFolder(String pathname) throws IOException, WebzException {
		return siteFileSystem.getOperations().createFolder(pathname);
	}

	@Override
	public FileSpecific uploadFile(String pathname, InputStream content, long numBytes) throws IOException, WebzException {
		return siteFileSystem.getOperations().uploadFile(pathname, content, numBytes);
	}

	@Override
	public FileSpecific uploadFile(String pathname, InputStream content) throws IOException, WebzException {
		return siteFileSystem.getOperations().uploadFile(pathname, content);
	}

	@Override
	public WebzMetadata move(String srcPathname, String destPathname) throws IOException, WebzException {
		return siteFileSystem.getOperations().move(srcPathname, destPathname);
	}

	@Override
	public WebzMetadata copy(String srcPathname, String destPathname) throws IOException, WebzException {
		return siteFileSystem.getOperations().copy(srcPathname, destPathname);
	}

	@Override
	public void delete(String pathname) throws IOException, WebzException {
		siteFileSystem.getOperations().delete(pathname);
	}

}
