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
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzMetadata;
import org.terems.webz.WebzMetadata.FileSpecific;
import org.terems.webz.base.WebzMetadataProxy;
import org.terems.webz.internals.FreshParentChildrenMetadata;
import org.terems.webz.internals.ParentChildrenMetadata;
import org.terems.webz.internals.WebzFileSystem;
import org.terems.webz.internals.WebzFileSystemStructure;
import org.terems.webz.internals.WebzPathNormalizer;
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

	private static class FileFound {

		WebzMetadata proxiedMetadata;
		WebzFileSystem host;
		String actualPathname;

		FileFound(WebzMetadata proxiedMetadata, WebzFileSystem host, String actualPathname) {
			this.proxiedMetadata = proxiedMetadata;
			this.host = host;
			this.actualPathname = actualPathname;
		}

	}

	private FileFound findFile(String pathname) throws IOException, WebzException {

		WebzPathNormalizer pathNormalizer = getPathNormalizer();
		WebzFileSystemStructure siteStructure = siteFileSystem.getStructure();
		WebzFileSystemStructure spaStructure = spaFileSystem.getStructure();

		WebzMetadata metadata = spaStructure.getMetadata(pathname);
		if (metadata != null) {
			return new FileFound(metadata, spaFileSystem, pathname);
		}
		metadata = siteStructure.getMetadata(pathname);
		if (metadata != null) {
			return new FileFound(metadata, siteFileSystem, pathname);
		}
		// TODO get rid of previous two checks when caching is implemented ?

		String currentPath = "";

		String[] pathMembers = pathNormalizer.splitPathname(pathname);
		for (int i = 0; i < pathMembers.length; i++) {

			boolean matchFound = false;
			Map<String, WebzMetadata> children = siteStructure.getChildPathnamesAndMetadata(currentPath);
			if (children != null) {
				for (Map.Entry<String, WebzMetadata> childEntry : children.entrySet()) {

					WebzMetadata childMetadata = childEntry.getValue();
					if (pathMembers[i].equals(childMetadata.getName())) {

						if (childMetadata.isFolder()) {
							matchFound = true;
							currentPath = pathNormalizer.concatPathname(currentPath, pathMembers[i]);
						}
						break;
					}
				}
			}
			if (!matchFound) {

				if (i > 0) {

					final String linkedPathname = pathNormalizer.constructPathname(pathMembers, i, pathMembers.length);
					final WebzMetadata metadataToProxy = spaStructure.getMetadata(linkedPathname);
					if (metadataToProxy != null) {

						return new FileFound(new WebzMetadataProxy() {

							@Override
							protected WebzMetadata getInnerMetadata() {
								return metadataToProxy;
							}

							@Override
							public String getLinkedPathname() {
								return linkedPathname;
							}

						}, spaFileSystem, linkedPathname);
					}
				}
				return null;
			}
		}
		return null;
	}

	@Override
	public WebzMetadata getMetadata(String pathname) throws IOException, WebzException {

		FileFound found = findFile(pathname);
		if (found == null) {
			return null;
		}
		return found.proxiedMetadata;
	}

	@Override
	public ParentChildrenMetadata getParentChildrenMetadata(String parentPathname) throws IOException, WebzException {

		// TODO if folder exists in both file systems then merge children
		FileFound found = findFile(parentPathname);
		if (found == null) {
			return null;
		}

		ParentChildrenMetadata result = found.host.getStructure().getParentChildrenMetadata(found.actualPathname);
		if (result != null) {
			result.parentMetadata = found.proxiedMetadata;
		}
		return result;
	}

	@Override
	public FreshParentChildrenMetadata getParentChildrenMetadataIfChanged(String parentPathname, Object previousFolderHash)
			throws IOException, WebzException {

		// TODO if folder exists in both file systems then merge children
		FileFound found = findFile(parentPathname);
		if (found == null) {
			return null;
		}

		FreshParentChildrenMetadata result = found.host.getStructure().getParentChildrenMetadataIfChanged(found.actualPathname,
				previousFolderHash);
		if (result != null && result.parentChildrenMetadata != null) {
			result.parentChildrenMetadata.parentMetadata = found.proxiedMetadata;
		}
		return result;
	}

	@Override
	public Map<String, WebzMetadata> getChildPathnamesAndMetadata(String parentPathname) throws IOException, WebzException {

		// TODO if folder exists in both file systems then merge children
		FileFound found = findFile(parentPathname);
		if (found == null) {
			return null;
		}
		return found.host.getStructure().getChildPathnamesAndMetadata(found.actualPathname);
	}

	@Override
	public Collection<String> getChildPathnames(String parentPathname) throws IOException, WebzException {

		// TODO if folder exists in both file systems then merge children
		FileFound found = findFile(parentPathname);
		if (found == null) {
			return null;
		}
		return found.host.getStructure().getChildPathnames(found.actualPathname);
	}

	@Override
	public WebzFileDownloader getFileDownloader(String pathname) throws IOException, WebzException {

		FileFound found = findFile(pathname);
		if (found == null) {
			return null;
		}

		WebzFileDownloader result = found.host.getOperations().getFileDownloader(found.actualPathname);
		if (result != null) {
			result.fileSpecific = found.proxiedMetadata.getFileSpecific();
		}
		return result;
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
