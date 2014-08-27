package org.terems.webz.impl.cache;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.ParentChildrenMetadata;
import org.terems.webz.WebzDefaults;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzFileSystem;
import org.terems.webz.WebzMetadata;
import org.terems.webz.base.BaseWebzFileSystem;
import org.terems.webz.cache.ChildPathNamesHolder;
import org.terems.webz.cache.FilePayloadHolder;
import org.terems.webz.cache.WebzFileSystemCache;
import org.terems.webz.util.WebzUtils;

// TODO background thread should periodically check certain number of pathNames against Dropbox to drop the whole FS cache if necessary
// TODO also do similar check when some metadata is being fetched as a "side-effect" in cache implementations to drop the whole FS cache if necessary
/** TODO !!! describe !!! **/
public class CachedFileSystem extends BaseWebzFileSystem {

	private static final Logger LOG = LoggerFactory.getLogger(CachedFileSystem.class);

	private WebzFileSystemCache cache;
	private WebzFileSystem innerFileSystem;

	private final String fileSystemUniqueId;

	public CachedFileSystem(WebzFileSystem innerFileSystem, WebzFileSystemCache cacheImplementation) {
		this(innerFileSystem, cacheImplementation, WebzDefaults.PAYLOAD_CACHE_THRESHOLD_BYTES);
	}

	public CachedFileSystem(WebzFileSystem innerFileSystem, WebzFileSystemCache cacheImplementation, int filePayloadSizeThreshold) {
		// TODO add two additional modes:
		// 1) payload cache disabled completely
		// 2) payload cache works for any payload sizes without the threshold

		if (innerFileSystem instanceof CachedFileSystem) {
			throw new IllegalArgumentException(
					"an instance of BaseFileSystemCache should not be wrapped with another instance of BaseFileSystemCache - an attempt was made to wrap "
							+ innerFileSystem.getFileSystemUniqueId() + " with an instance of " + getClass());
		}

		cacheImplementation.init(innerFileSystem, filePayloadSizeThreshold);

		this.cache = cacheImplementation;
		this.innerFileSystem = innerFileSystem;

		this.fileSystemUniqueId = cacheImplementation.getCacheTypeName() + "-for-" + innerFileSystem.getFileSystemUniqueId();

		LOG.info("'" + this.fileSystemUniqueId + "' file system cache was created to wrap '" + innerFileSystem.getFileSystemUniqueId()
				+ "'");
	}

	@Override
	public String getFileSystemUniqueId() {
		return fileSystemUniqueId;
	}

	@Override
	public String normalizePathName(String pathName) {
		return innerFileSystem.normalizePathName(pathName);
	}

	@Override
	public String getParentPathName(String pathName) {
		return innerFileSystem.getParentPathName(pathName);
	}

	@Override
	public void inflate(WebzFile file) throws IOException, WebzException {
		innerFileSystem.inflate(cache, file);
	}

	private void dropFilePayloadAndChildPathNames(String pathName) {

		cache.dropChildPathNamesHolderFromCache(pathName);
		// TODO drop whole sub-tree (mind possible gaps in cache caused by evictions - maybe configure child path names cache to
		// not evict anything ever at all?) - think in which cases this operation should be done

		cache.dropFilePayloadHolderFromCache(pathName);
	}

	private void dropPathNameInCaches(String pathName) {
		cache.dropMetadataFromCache(pathName);
		dropFilePayloadAndChildPathNames(pathName);
	}

	private void dropPathNameInCachesAndUpdateMetadata(String pathName, WebzMetadata metadata) {

		if (metadata == null) {
			dropPathNameInCaches(pathName);
		} else {
			cache.putMetadataIntoCache(pathName, metadata);

			dropFilePayloadAndChildPathNames(pathName);
		}
	}

	@Override
	public WebzMetadata getMetadata(String pathName) throws IOException, WebzException {
		return cache.fetchMetadata(pathName);
	}

	@Override
	public ParentChildrenMetadata getParentChildrenMetadata(String parentPathName) throws IOException, WebzException {

		ChildPathNamesHolder childPathNamesHolder = cache.fetchChildPathNamesHolder(parentPathName);
		if (childPathNamesHolder == null) {
			return null;
		}

		ParentChildrenMetadata parentChildrenMetadata = new ParentChildrenMetadata();
		parentChildrenMetadata.folderHash = childPathNamesHolder.folderHash;

		parentChildrenMetadata.parentMetadata = cache.fetchMetadata(parentPathName);
		parentChildrenMetadata.childPathNamesAndMetadata = cache.fetchMetadata(childPathNamesHolder.childPathNames);

		return parentChildrenMetadata;
	}

	@Override
	public Map<String, WebzMetadata> getChildPathNamesAndMetadata(String parentPathName) throws IOException, WebzException {
		ChildPathNamesHolder childPathNamesHolder = cache.fetchChildPathNamesHolder(parentPathName);
		return childPathNamesHolder == null ? null : cache.fetchMetadata(childPathNamesHolder.childPathNames);
	}

	@Override
	public Collection<String> getChildPathNames(String parentPathName) throws IOException, WebzException {
		ChildPathNamesHolder childPathNamesHolder = cache.fetchChildPathNamesHolder(parentPathName);
		return childPathNamesHolder == null ? null : childPathNamesHolder.childPathNames;
	}

	private void writeFilePayload(FilePayloadHolder payloadHolder, String pathName, OutputStream out) throws IOException, WebzException {
		if (payloadHolder.payload != null) {
			payloadHolder.payload.writeTo(out);
			// TODO drop payload in a thread-safe manner if it is bigger than the threshold + log-warn? about this + think if
			// all caches should be dropped in such a case
		} else {
			if (LOG.isTraceEnabled()) {
				WebzUtils.traceFSMessage(LOG, "PAYLOAD for '" + pathName + "' is being fetched without being cached", this);
			}
			innerFileSystem.fileContentToOutputStream(pathName, out);
			// TODO should javax.servlet.AsyncContext be used ?
		}
	}

	private WebzMetadata.FileSpecific fetchFileSpecific(final String pathName) throws IOException, WebzException {

		WebzMetadata.FileSpecific fileSpecific = cache.fetchMetadata(pathName).getFileSpecific();
		if (fileSpecific == null) {
			throw new WebzException("'" + pathName + "' is not a file");
		}

		return fileSpecific;
	}

	@Override
	public WebzMetadata.FileSpecific fileContentToOutputStream(String pathName, OutputStream out) throws IOException, WebzException {

		FilePayloadHolder payloadHolder = cache.fetchFilePayloadHolder(pathName);
		if (payloadHolder == null) {
			return null;
		}

		WebzMetadata.FileSpecific fileSpecific = fetchFileSpecific(pathName);
		writeFilePayload(payloadHolder, pathName, out);
		return fileSpecific;
	}

	@Override
	public WebzFileDownloader getFileContentDownloader(final String pathName) throws IOException, WebzException {

		final FilePayloadHolder payloadHolder = cache.fetchFilePayloadHolder(pathName);
		if (payloadHolder == null) {
			return null;
		}

		return new WebzFileDownloader(fetchFileSpecific(pathName)) {

			@Override
			public void fileContentToOutputStream(OutputStream out) throws IOException, WebzException {
				writeFilePayload(payloadHolder, pathName, out);
			}
		};
	}

	@Override
	public WebzMetadata createFolder(String pathName) throws IOException, WebzException {

		WebzMetadata metadata = innerFileSystem.createFolder(pathName);

		dropPathNameInCachesAndUpdateMetadata(pathName, metadata);
		return metadata;
	}

	@Override
	public WebzMetadata.FileSpecific uploadFile(String pathName, byte[] content) throws IOException, WebzException {

		WebzMetadata.FileSpecific fileSpecific = innerFileSystem.uploadFile(pathName, content);

		dropPathNameInCachesAndUpdateMetadata(pathName, fileSpecific);
		return fileSpecific;
	}

	@Override
	public WebzMetadata move(String srcPathName, String destPathName) throws IOException, WebzException {

		WebzMetadata metadata = innerFileSystem.move(srcPathName, destPathName);

		dropPathNameInCaches(srcPathName);
		dropPathNameInCachesAndUpdateMetadata(destPathName, metadata);
		return metadata;
	}

	@Override
	public WebzMetadata copy(String srcPathName, String destPathName) throws IOException, WebzException {

		WebzMetadata metadata = innerFileSystem.copy(srcPathName, destPathName);

		dropPathNameInCachesAndUpdateMetadata(destPathName, metadata);
		return metadata;
	}

	@Override
	public void delete(String pathName) throws IOException, WebzException {

		innerFileSystem.delete(pathName);

		dropPathNameInCaches(pathName);
	}

}
