/*
 * WebZ Server can serve web pages from various local and remote file sources.
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzMetadata;
import org.terems.webz.WebzMetadata.FileSpecific;
import org.terems.webz.WebzProperties;
import org.terems.webz.base.BaseWebzDestroyable;
import org.terems.webz.internals.FreshParentChildrenMetadata;
import org.terems.webz.internals.ParentChildrenMetadata;
import org.terems.webz.internals.WebzFileSystemCache;
import org.terems.webz.internals.WebzFileSystemImpl;
import org.terems.webz.internals.WebzObjectFactory;
import org.terems.webz.internals.WebzPathNormalizer;
import org.terems.webz.util.WebzUtils;

public class WebzFileSystemImplTracer extends BaseWebzDestroyable implements WebzFileSystemImpl {

	private static final Logger LOG = LoggerFactory.getLogger(WebzFileSystemImplTracer.class);

	private WebzFileSystemImpl fsImpl;

	public static WebzFileSystemImpl wrapIfApplicable(WebzFileSystemImpl fsImpl) {

		if (LOG.isTraceEnabled()) {
			return new WebzFileSystemImplTracer(fsImpl);
		}
		return fsImpl;
	}

	private WebzFileSystemImplTracer(WebzFileSystemImpl fsImpl) {
		this.fsImpl = fsImpl;
	}

	@Override
	public void inflate(WebzFile file) throws IOException, WebzException {

		LOG.trace(WebzUtils.formatFileSystemMessageNoBrackets(".inflate('" + file.getPathname() + "');", fsImpl));
		fsImpl.inflate(file);
	}

	@Override
	public void inflate(WebzFileSystemCache fsCache, WebzFile file) throws IOException, WebzException {

		LOG.trace(WebzUtils.formatFileSystemMessageNoBrackets(
				"attempt to inflate '" + file.getPathname() + "' in " + fsCache.getCacheTypeName(), fsImpl));
		fsImpl.inflate(fsCache, file);
	}

	@Override
	public WebzMetadata getMetadata(String pathname) throws IOException, WebzException {

		LOG.trace(WebzUtils.formatFileSystemMessageNoBrackets(".getMetadata('" + pathname + "');", fsImpl));
		return fsImpl.getMetadata(pathname);
	}

	private static final String FOLDER_HASH_MSG = "folder hash";
	private static final String NOT_FOUND_MSG = "NOT FOUND";

	@Override
	public ParentChildrenMetadata getParentChildrenMetadata(String parentPathname) throws IOException, WebzException {

		LOG.trace(WebzUtils.formatFileSystemMessageNoBrackets(".getParentChildrenMetadata('" + parentPathname + "');", fsImpl));
		ParentChildrenMetadata result = fsImpl.getParentChildrenMetadata(parentPathname);

		if (result == null) {
			LOG.trace(WebzUtils.formatFileSystemMessage("'" + parentPathname + "' " + NOT_FOUND_MSG, fsImpl));
		} else if (result.parentMetadata == null || result.parentMetadata.isFolder()) {
			LOG.trace(WebzUtils.formatFileSystemMessage(
					"'" + parentPathname + "' - " + FOLDER_HASH_MSG + ": '" + String.valueOf(result.folderHash) + "'", fsImpl));
		}
		return result;
	}

	@Override
	public FreshParentChildrenMetadata getParentChildrenMetadataIfChanged(String parentPathname, Object previousFolderHash)
			throws IOException, WebzException {

		LOG.trace(WebzUtils.formatFileSystemMessageNoBrackets(".getParentChildrenMetadataIfChanged('" + parentPathname
				+ (previousFolderHash == null ? "', null !!! );" : "', '" + previousFolderHash + "');"), fsImpl));
		FreshParentChildrenMetadata result = fsImpl.getParentChildrenMetadataIfChanged(parentPathname, previousFolderHash);

		if (result == null) {
			LOG.trace(WebzUtils.formatFileSystemMessage("'" + parentPathname + "' not changed - " + FOLDER_HASH_MSG + ": '"
					+ previousFolderHash + "'", fsImpl));
		} else if (result.parentChildrenMetadata == null) {
			LOG.trace(WebzUtils.formatFileSystemMessage("'" + parentPathname + "' " + NOT_FOUND_MSG, fsImpl));
		} else {
			LOG.trace(WebzUtils.formatFileSystemMessage(
					"'" + parentPathname + "' - " + FOLDER_HASH_MSG + ": '" + String.valueOf(result.parentChildrenMetadata.folderHash)
							+ "'", fsImpl));
		}
		return result;
	}

	@Override
	public Map<String, WebzMetadata> getChildPathnamesAndMetadata(String parentPathname) throws IOException, WebzException {

		LOG.trace(WebzUtils.formatFileSystemMessageNoBrackets(".getChildPathnamesAndMetadata('" + parentPathname + "');", fsImpl));
		return fsImpl.getChildPathnamesAndMetadata(parentPathname);
	}

	@Override
	public Collection<String> getChildPathnames(String parentPathname) throws IOException, WebzException {

		LOG.trace(WebzUtils.formatFileSystemMessageNoBrackets(".getChildPathnames('" + parentPathname + "');", fsImpl));
		return fsImpl.getChildPathnames(parentPathname);
	}

	@Override
	public WebzFileDownloader getFileDownloader(String pathname) throws IOException, WebzException {

		LOG.trace(WebzUtils.formatFileSystemMessageNoBrackets(".getFileDownloader('" + pathname + "');", fsImpl));
		return fsImpl.getFileDownloader(pathname);
	}

	@Override
	public WebzMetadata createFolder(String pathname) throws IOException, WebzException {

		LOG.trace(WebzUtils.formatFileSystemMessageNoBrackets(".createFolder('" + pathname + "');", fsImpl));
		return fsImpl.createFolder(pathname);
	}

	@Override
	public FileSpecific uploadFile(String pathname, InputStream content, long numBytes) throws IOException, WebzException {

		LOG.trace(WebzUtils.formatFileSystemMessageNoBrackets(".uploadFile('" + pathname + "', " + numBytes + " bytes);", fsImpl));
		return fsImpl.uploadFile(pathname, content, numBytes);
	}

	@Override
	public FileSpecific uploadFile(String pathname, InputStream content) throws IOException, WebzException {

		LOG.trace(WebzUtils.formatFileSystemMessageNoBrackets(".uploadFile('" + pathname + "');", fsImpl));
		return fsImpl.uploadFile(pathname, content);
	}

	@Override
	public WebzMetadata move(String srcPathname, String destPathname) throws IOException, WebzException {

		LOG.trace(WebzUtils.formatFileSystemMessageNoBrackets(".move('" + srcPathname + "', '" + destPathname + "');", fsImpl));
		return fsImpl.move(srcPathname, destPathname);
	}

	@Override
	public WebzMetadata copy(String srcPathname, String destPathname) throws IOException, WebzException {

		LOG.trace(WebzUtils.formatFileSystemMessageNoBrackets(".copy('" + srcPathname + "', '" + destPathname + "');", fsImpl));
		return fsImpl.copy(srcPathname, destPathname);
	}

	@Override
	public void delete(String pathname) throws IOException, WebzException {

		LOG.trace(WebzUtils.formatFileSystemMessageNoBrackets(".delete('" + pathname + "');", fsImpl));
		fsImpl.delete(pathname);
	}

	@Override
	public String getUniqueId() {
		return fsImpl.getUniqueId();
	}

	@Override
	public void init(WebzPathNormalizer pathNormalizer, WebzProperties properties, WebzObjectFactory factory) throws WebzException {
		fsImpl.init(pathNormalizer, properties, factory);
	}

}
