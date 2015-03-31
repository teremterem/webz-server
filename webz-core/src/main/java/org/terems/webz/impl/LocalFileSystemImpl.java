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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzMetadata;
import org.terems.webz.WebzProperties;
import org.terems.webz.internals.ParentChildrenMetadata;
import org.terems.webz.internals.WebzPathNormalizer;
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

		Map<String, WebzMetadata> childPathnamesAndMetadata = null;

		String[] children = file.list();
		if (children != null) {
			WebzPathNormalizer pathNormalizer = getPathNormalizer();

			String localBasePath = file.getAbsolutePath();
			childPathnamesAndMetadata = new LinkedHashMap<String, WebzMetadata>();

			for (String childName : children) {
				File child = new File(localBasePath, childName);
				childPathnamesAndMetadata.put(pathNormalizer.concatPathname(parentPathname, childName), new LocalFileMetadata(child));
			}
		}
		return new ParentChildrenMetadata(new LocalFileMetadata(file), childPathnamesAndMetadata, null);
	}

	@Override
	public Set<String> getChildPathnames(String parentPathname) throws IOException, WebzException {

		File file = new File(basePath, parentPathname);
		if (!fileExists(file, parentPathname)) {
			return null;
		}

		String[] children = file.list();
		if (children == null) {
			return null;
		}

		WebzPathNormalizer pathNormalizer = getPathNormalizer();
		Set<String> childPathnames = new LinkedHashSet<String>();
		for (String childName : children) {
			childPathnames.add(pathNormalizer.concatPathname(parentPathname, childName));
		}
		return childPathnames;
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

}
