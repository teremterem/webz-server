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

package org.terems.webz.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzMetadata;
import org.terems.webz.WebzMetadata.FileSpecific;
import org.terems.webz.WebzProperties;
import org.terems.webz.internals.ParentChildrenMetadata;
import org.terems.webz.internals.base.BaseWebzFileSystemImpl;

public class LocalFileSystemImpl extends BaseWebzFileSystemImpl {

	private static final Logger LOG = LoggerFactory.getLogger(LocalFileSystemImpl.class);

	private String basePath;
	private String uniqueId;

	@Override
	protected void init() {

		basePath = getPathNormalizer().normalizePathname(getProperties().get(WebzProperties.FS_BASE_PATH_PROPERTY), false);
		uniqueId = "localhost-" + basePath;

		if (LOG.isInfoEnabled()) {
			LOG.info("'" + uniqueId + "' file system was created");
		}
	}

	@Override
	public String getUniqueId() {
		return uniqueId;
	}

	@Override
	public WebzMetadata getMetadata(String pathname) throws IOException, WebzException {

		File file = new File(basePath, pathname);
		return fileExists(file, pathname) ? new LocalFileMetadata(file) : null;
	}

	@Override
	public ParentChildrenMetadata getParentChildrenMetadata(String parentPathname) throws IOException, WebzException {

		File file = new File(basePath, parentPathname);
		if (!fileExists(file, parentPathname)) {
			return null;
		}

		ParentChildrenMetadata parentChildren = new ParentChildrenMetadata();
		parentChildren.parentMetadata = new LocalFileMetadata(file);

		String[] children = file.list();
		if (children != null) {

			String localBasePath = file.getAbsolutePath();
			Map<String, WebzMetadata> pathnamesAndMetadata = new HashMap<String, WebzMetadata>();
			parentChildren.childPathnamesAndMetadata = pathnamesAndMetadata;
			for (String childName : children) {
				File child = new File(localBasePath, childName);
				pathnamesAndMetadata.put(getPathNormalizer().concatPathname(parentPathname, childName), new LocalFileMetadata(child));
			}
		}
		return parentChildren;
	}

	@Override
	public WebzFileDownloader getFileDownloader(String pathname) throws IOException, WebzException {

		File file = new File(basePath, pathname);
		if (!fileExists(file, pathname) || !file.isFile()) {
			return null;
		}

		return new WebzFileDownloader(new LocalFileMetadata(file).getFileSpecific(), new FileInputStream(file));
	}

	protected boolean pathnameMatchesFileExactly(String pathnameToValidate, File file) throws IOException {
		return getPathNormalizer().normalizePathname(file.getCanonicalPath(), false).endsWith(pathnameToValidate);
	}

	protected boolean fileExists(File file, String pathnameToValidate) throws IOException {
		return file.exists() && pathnameMatchesFileExactly(pathnameToValidate, file);
	}

	// TODO implement "operations" part of LocalFileSystem

	@Override
	public WebzMetadata createFolder(String pathname) throws IOException, WebzException {
		throw new UnsupportedOperationException();
	}

	@Override
	public FileSpecific uploadFile(String pathname, InputStream content, long numBytes) throws IOException, WebzException {
		throw new UnsupportedOperationException();
	}

	@Override
	public FileSpecific uploadFile(String pathname, InputStream content) throws IOException, WebzException {
		throw new UnsupportedOperationException();
	}

	@Override
	public WebzMetadata move(String srcPathname, String destPathname) throws IOException, WebzException {
		throw new UnsupportedOperationException();
	}

	@Override
	public WebzMetadata copy(String srcPathname, String destPathname) throws IOException, WebzException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(String pathname) throws IOException, WebzException {
		throw new UnsupportedOperationException();
	}

}
