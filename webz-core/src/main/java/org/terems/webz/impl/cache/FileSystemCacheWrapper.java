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

package org.terems.webz.impl.cache;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzDefaults;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzMetadata;
import org.terems.webz.WebzProperties;
import org.terems.webz.internals.ParentChildrenMetadata;
import org.terems.webz.internals.WebzFileSystemCache;
import org.terems.webz.internals.WebzFileSystemImpl;
import org.terems.webz.internals.WebzObjectFactory;
import org.terems.webz.internals.WebzPathNormalizer;
import org.terems.webz.internals.base.BaseWebzFileSystemImpl;
import org.terems.webz.internals.cache.ChildPathnamesHolder;
import org.terems.webz.internals.cache.FileContentHolder;
import org.terems.webz.util.WebzUtils;

// TODO background thread should periodically check cached pathnames against Dropbox to drop the entire cache if necessary
// TODO make sure to check the cache against metadata fetched by "side-effect" in order to also drop the entire cache if necessary
public class FileSystemCacheWrapper extends BaseWebzFileSystemImpl {

	private static final Logger LOG = LoggerFactory.getLogger(FileSystemCacheWrapper.class);

	private WebzFileSystemImpl fileSystemImpl;
	private WebzFileSystemCache cacheImpl;

	private int filePayloadSizeThreshold;

	private String uniqueId;

	public FileSystemCacheWrapper init(WebzFileSystemImpl fileSystemImpl, WebzPathNormalizer pathNormalizer, WebzProperties properties,
			WebzObjectFactory factory) throws WebzException {

		// TODO additional mode #1: payload cache disabled completely
		// TODO additional mode #2: payload cache works for any payload sizes without the threshold ?

		if (fileSystemImpl instanceof FileSystemCacheWrapper) {
			throw new IllegalArgumentException("an instance of " + FileSystemCacheWrapper.class.getSimpleName()
					+ " should not be wrapped with another instance of " + FileSystemCacheWrapper.class.getSimpleName()
					+ " - an attempt was made to wrap '" + fileSystemImpl.getUniqueId() + "' with an instance of " + getClass());
		}
		this.fileSystemImpl = fileSystemImpl;

		filePayloadSizeThreshold = Integer.valueOf(properties.get(WebzProperties.FS_CACHE_PAYLOAD_THRESHOLD_BYTES_PROPERTY,
				String.valueOf(WebzDefaults.FS_CACHE_PAYLOAD_THRESHOLD_BYTES)));
		cacheImpl = ((WebzFileSystemCache) factory.newDestroyable(properties.get(WebzProperties.FS_CACHE_IMPL_CLASS_PROPERTY,
				WebzDefaults.FS_CACHE_IMPL_CLASS))).init(fileSystemImpl, filePayloadSizeThreshold);

		uniqueId = cacheImpl.getCacheTypeName() + "-for-" + fileSystemImpl.getUniqueId();

		if (LOG.isInfoEnabled()) {
			LOG.info("'" + uniqueId + "' file system cache wrapper was created for '" + fileSystemImpl.getUniqueId() + "'");
		}
		super.init(pathNormalizer, properties, factory);
		return this;
	}

	@Override
	public String getUniqueId() {
		return uniqueId;
	}

	@Override
	public void inflate(WebzFile file) throws IOException, WebzException {
		fileSystemImpl.inflate(cacheImpl, file);
	}

	@Override
	public WebzMetadata getMetadata(String pathname) {
		return cacheImpl.fetchMetadata(pathname);
	}

	@Override
	public ParentChildrenMetadata getParentChildrenMetadata(String parentPathname) {

		ChildPathnamesHolder childPathnamesHolder = cacheImpl.fetchChildPathnamesHolder(parentPathname);
		if (childPathnamesHolder == null) {
			return null;
		}
		return new ParentChildrenMetadata(cacheImpl.fetchMetadata(parentPathname),
				cacheImpl.fetchMetadata(childPathnamesHolder.childPathnames), childPathnamesHolder.folderHash);
	}

	@Override
	public Map<String, WebzMetadata> getChildPathnamesAndMetadata(String parentPathname) {
		ChildPathnamesHolder childPathnamesHolder = cacheImpl.fetchChildPathnamesHolder(parentPathname);
		return childPathnamesHolder == null ? null : cacheImpl.fetchMetadata(childPathnamesHolder.childPathnames);
	}

	@Override
	public Set<String> getChildPathnames(String parentPathname) {
		ChildPathnamesHolder childPathnamesHolder = cacheImpl.fetchChildPathnamesHolder(parentPathname);
		return childPathnamesHolder == null ? null : childPathnamesHolder.childPathnames;
	}

	private WebzMetadata.FileSpecific fetchFileSpecific(String pathname) throws IOException, WebzException {
		WebzMetadata metadata = cacheImpl.fetchMetadata(pathname);
		return metadata == null ? null : metadata.getFileSpecific();
	}

	@Override
	public WebzFileDownloader getFileDownloader(String pathname) throws IOException, WebzException {

		FileContentHolder payloadHolder = cacheImpl.fetchFileContentHolder(pathname);
		if (payloadHolder == null) {
			return null;
		}

		if (payloadHolder.content == null) {

			if (LOG.isTraceEnabled()) {
				LOG.trace(WebzUtils.formatFileSystemMessage("PAYLOAD for '" + pathname + "' is being fetched without being cached", this));
			}
			return fileSystemImpl.getFileDownloader(pathname);
		} else {

			if (payloadHolder.content.size() > filePayloadSizeThreshold) {
				if (LOG.isWarnEnabled()) {
					LOG.warn(WebzUtils.formatFileSystemMessage("payload for '" + pathname
							+ "' was cached even though it is bigger than the threshold - payload threshold (bytes): "
							+ filePayloadSizeThreshold + "; actual file size (bytes): " + payloadHolder.content.size()
							+ " - removing it from cache...", this));
				}
				cacheImpl.putFileContentHolderIntoCache(pathname, new FileContentHolder()); // putting empty content holder instead...
			}

			WebzMetadata.FileSpecific fileSpecific = fetchFileSpecific(pathname);
			if (fileSpecific == null) {

				cacheImpl.putFileContentHolderIntoCache(pathname, new FileContentHolder()); // putting empty content holder instead...
				return null;
			}

			return new WebzFileDownloader(fileSpecific, payloadHolder.content.createInputStream());
		}
	}

	// @Override
	// public WebzMetadata createFolder(String pathname) throws IOException, WebzException {
	//
	// WebzMetadata metadata = fileSystemImpl.createFolder(pathname);
	//
	// dropPathnameInCachesAndUpdateMetadata(pathname, metadata);
	// return metadata;
	// }
	//
	// @Override
	// public WebzMetadata.FileSpecific uploadFile(String pathname, InputStream content, long numBytes) throws IOException, WebzException {
	//
	// WebzMetadata.FileSpecific fileSpecific = fileSystemImpl.uploadFile(pathname, content, numBytes);
	//
	// dropPathnameInCachesAndUpdateMetadata(pathname, fileSpecific);
	// return fileSpecific;
	// }
	//
	// @Override
	// public WebzMetadata.FileSpecific uploadFile(String pathname, InputStream content) throws IOException, WebzException {
	//
	// WebzMetadata.FileSpecific fileSpecific = fileSystemImpl.uploadFile(pathname, content);
	//
	// dropPathnameInCachesAndUpdateMetadata(pathname, fileSpecific);
	// return fileSpecific;
	// }
	//
	// @Override
	// public WebzMetadata move(String srcPathname, String destPathname) throws IOException, WebzException {
	//
	// WebzMetadata metadata = fileSystemImpl.move(srcPathname, destPathname);
	//
	// dropPathnameInCaches(srcPathname);
	// dropPathnameInCachesAndUpdateMetadata(destPathname, metadata);
	// return metadata;
	// }
	//
	// @Override
	// public WebzMetadata copy(String srcPathname, String destPathname) throws IOException, WebzException {
	//
	// WebzMetadata metadata = fileSystemImpl.copy(srcPathname, destPathname);
	//
	// dropPathnameInCachesAndUpdateMetadata(destPathname, metadata);
	// return metadata;
	// }
	//
	// @Override
	// public void delete(String pathname) throws IOException, WebzException {
	//
	// fileSystemImpl.delete(pathname);
	//
	// dropPathnameInCaches(pathname);
	// }
	//
	// private void dropFileContentAndChildPathnames(String pathname) {
	//
	// cacheImpl.dropChildPathnamesHolderFromCache(pathname);
	// // TODO drop whole sub-tree (mind possible gaps in cache caused by evictions)
	//
	// cacheImpl.dropFileContentHolderFromCache(pathname);
	// }
	//
	// private void dropPathnameInCaches(String pathname) {
	// cacheImpl.dropMetadataFromCache(pathname);
	// dropFileContentAndChildPathnames(pathname);
	// }
	//
	// private void dropPathnameInCachesAndUpdateMetadata(String pathname, WebzMetadata metadata) {
	//
	// if (metadata == null) {
	// dropPathnameInCaches(pathname);
	// } else {
	// cacheImpl.putMetadataIntoCache(pathname, metadata);
	//
	// dropFileContentAndChildPathnames(pathname);
	// }
	// }

}
