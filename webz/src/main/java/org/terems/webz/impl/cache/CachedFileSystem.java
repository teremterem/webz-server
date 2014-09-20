package org.terems.webz.impl.cache;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.ParentChildrenMetadata;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzFileSystem;
import org.terems.webz.WebzMetadata;
import org.terems.webz.base.BaseWebzFileSystem;
import org.terems.webz.cache.ChildPathNamesHolder;
import org.terems.webz.cache.FileContentHolder;
import org.terems.webz.cache.WebzByteArrayInputStream;
import org.terems.webz.cache.WebzFileSystemCache;
import org.terems.webz.util.WebzUtils;

// TODO background thread should periodically check certain number of pathNames against Dropbox to drop the whole FS cache if necessary
// TODO also do similar check when some metadata is being fetched as a "side-effect" in cache implementations to drop the whole FS cache if necessary
/** TODO !!! describe !!! **/
public class CachedFileSystem extends BaseWebzFileSystem {

	private static final Logger LOG = LoggerFactory.getLogger(CachedFileSystem.class);

	private WebzFileSystem innerFileSystem;
	private WebzFileSystemCache cacheImpl;

	private final int filePayloadSizeThreshold;

	private final String fileSystemUniqueId;

	public CachedFileSystem(WebzFileSystem innerFileSystem, WebzFileSystemCache cacheImpl, int filePayloadSizeThreshold) {
		// TODO add two additional modes:
		// 1) payload cache disabled completely
		// 2) payload cache works for any payload sizes without the threshold

		if (innerFileSystem instanceof CachedFileSystem) {
			throw new IllegalArgumentException(
					"an instance of BaseFileSystemCache should not be wrapped with another instance of BaseFileSystemCache - an attempt was made to wrap "
							+ innerFileSystem.getFileSystemUniqueId() + " with an instance of " + getClass());
		}
		this.innerFileSystem = innerFileSystem;

		cacheImpl.init(innerFileSystem, filePayloadSizeThreshold);
		this.cacheImpl = cacheImpl;

		this.filePayloadSizeThreshold = filePayloadSizeThreshold;

		this.fileSystemUniqueId = cacheImpl.getCacheTypeName() + "-for-" + innerFileSystem.getFileSystemUniqueId();

		LOG.info("'" + this.fileSystemUniqueId + "' file system cache was created to wrap '" + innerFileSystem.getFileSystemUniqueId()
				+ "'");
	}

	@Override
	public void init(Properties properties) {
		// do nothing - all the initialization happens in constructor
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
	public boolean isNormalizedPathNameInvalid(String pathName) {
		return innerFileSystem.isNormalizedPathNameInvalid(pathName);
	}

	@Override
	public String getParentPathName(String pathName) {
		return innerFileSystem.getParentPathName(pathName);
	}

	@Override
	public String concatPathName(String basePathName, String relativePathName) {
		return innerFileSystem.concatPathName(basePathName, relativePathName);
	}

	@Override
	public void inflate(WebzFile file) throws IOException, WebzException {
		innerFileSystem.inflate(cacheImpl, file);
	}

	private void dropFileContentAndChildPathNames(String pathName) {

		cacheImpl.dropChildPathNamesHolderFromCache(pathName);
		// TODO drop whole sub-tree (mind possible gaps in cache caused by evictions - maybe configure child path names cache to
		// not evict anything ever at all?) - think in which cases this operation should be done

		cacheImpl.dropFileContentHolderFromCache(pathName);
	}

	private void dropPathNameInCaches(String pathName) {
		cacheImpl.dropMetadataFromCache(pathName);
		dropFileContentAndChildPathNames(pathName);
	}

	private void dropPathNameInCachesAndUpdateMetadata(String pathName, WebzMetadata metadata) {

		if (metadata == null) {
			dropPathNameInCaches(pathName);
		} else {
			cacheImpl.putMetadataIntoCache(pathName, metadata);

			dropFileContentAndChildPathNames(pathName);
		}
	}

	@Override
	public WebzMetadata getMetadata(String pathName) {
		return cacheImpl.fetchMetadata(pathName);
	}

	@Override
	public ParentChildrenMetadata getParentChildrenMetadata(String parentPathName) {

		ChildPathNamesHolder childPathNamesHolder = cacheImpl.fetchChildPathNamesHolder(parentPathName);
		if (childPathNamesHolder == null) {
			return null;
		}

		ParentChildrenMetadata parentChildrenMetadata = new ParentChildrenMetadata();
		parentChildrenMetadata.folderHash = childPathNamesHolder.folderHash;

		parentChildrenMetadata.parentMetadata = cacheImpl.fetchMetadata(parentPathName);
		parentChildrenMetadata.childPathNamesAndMetadata = cacheImpl.fetchMetadata(childPathNamesHolder.childPathNames);

		return parentChildrenMetadata;
	}

	@Override
	public Map<String, WebzMetadata> getChildPathNamesAndMetadata(String parentPathName) {
		ChildPathNamesHolder childPathNamesHolder = cacheImpl.fetchChildPathNamesHolder(parentPathName);
		return childPathNamesHolder == null ? null : cacheImpl.fetchMetadata(childPathNamesHolder.childPathNames);
	}

	@Override
	public Collection<String> getChildPathNames(String parentPathName) {
		ChildPathNamesHolder childPathNamesHolder = cacheImpl.fetchChildPathNamesHolder(parentPathName);
		return childPathNamesHolder == null ? null : childPathNamesHolder.childPathNames;
	}

	private WebzMetadata.FileSpecific fetchFileSpecific(String pathName) throws IOException, WebzException {
		WebzMetadata metadata = cacheImpl.fetchMetadata(pathName);
		return metadata == null ? null : metadata.getFileSpecific();
	}

	@Override
	public WebzFileDownloader getFileDownloader(String pathName) throws IOException, WebzException {

		FileContentHolder payloadHolder = cacheImpl.fetchFileContentHolder(pathName);
		if (payloadHolder == null) {
			return null;
		}

		if (payloadHolder.content == null) {

			if (LOG.isTraceEnabled()) {
				LOG.trace(WebzUtils.formatFileSystemMessage("PAYLOAD for '" + pathName + "' is being fetched without being cached", this));
			}
			return innerFileSystem.getFileDownloader(pathName);
		} else {

			if (payloadHolder.content.size() > filePayloadSizeThreshold) {
				if (LOG.isWarnEnabled()) {
					LOG.warn(WebzUtils.formatFileSystemMessage("payload for '" + pathName
							+ "' was cached even though it is bigger than the threshold - payload threshold (bytes): "
							+ filePayloadSizeThreshold + "; actual file size (bytes): " + payloadHolder.content.size()
							+ " - removing it from cache...", this));
				}
				cacheImpl.putFileContentHolderIntoCache(pathName, new FileContentHolder()); // putting empty content holder instead...
				// TODO think if all caches should be dropped in such a case
			}

			WebzMetadata.FileSpecific fileSpecific = fetchFileSpecific(pathName);
			if (fileSpecific == null) {
				// TODO think if all caches (or this particular entry ?) should be dropped in such a case
				return null;
			}

			final WebzByteArrayInputStream webzIn = payloadHolder.content.createInputStream();
			return new WebzFileDownloader(fileSpecific, webzIn) {

				@Override
				protected long copyContent(OutputStream out) throws IOException {
					return webzIn.writeAvailableToOutputStream(out);
				}
			};
		}
	}

	@Override
	public WebzMetadata createFolder(String pathName) throws IOException, WebzException {

		WebzMetadata metadata = innerFileSystem.createFolder(pathName);

		dropPathNameInCachesAndUpdateMetadata(pathName, metadata);
		return metadata;
	}

	@Override
	public WebzMetadata.FileSpecific uploadFile(String pathName, InputStream content, long numBytes) throws IOException, WebzException {

		WebzMetadata.FileSpecific fileSpecific = innerFileSystem.uploadFile(pathName, content, numBytes);

		dropPathNameInCachesAndUpdateMetadata(pathName, fileSpecific);
		return fileSpecific;
	}

	@Override
	public WebzMetadata.FileSpecific uploadFile(String pathName, InputStream content) throws IOException, WebzException {

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
