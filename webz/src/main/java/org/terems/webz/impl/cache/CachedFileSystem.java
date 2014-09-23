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
import org.terems.webz.cache.ChildPathnamesHolder;
import org.terems.webz.cache.FileContentHolder;
import org.terems.webz.cache.WebzByteArrayInputStream;
import org.terems.webz.cache.WebzFileSystemCache;
import org.terems.webz.util.WebzUtils;

// TODO background thread should periodically check certain number of pathnames against Dropbox to drop the whole FS cache if necessary
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

		if (LOG.isInfoEnabled()) {
			LOG.info("'" + this.fileSystemUniqueId + "' file system cache was created to wrap '" + innerFileSystem.getFileSystemUniqueId()
					+ "'");
		}
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
	public String normalizePathname(String pathname) {
		return innerFileSystem.normalizePathname(pathname);
	}

	@Override
	public boolean isNormalizedPathnameInvalid(String pathname) {
		return innerFileSystem.isNormalizedPathnameInvalid(pathname);
	}

	@Override
	public String getParentPathname(String pathname) {
		return innerFileSystem.getParentPathname(pathname);
	}

	@Override
	public String concatPathname(String basePath, String relativePathname) {
		return innerFileSystem.concatPathname(basePath, relativePathname);
	}

	@Override
	public boolean belongsToSubtree(String pathname, String subtreePath) {
		return innerFileSystem.belongsToSubtree(pathname, subtreePath);
	}

	@Override
	public void inflate(WebzFile file) throws IOException, WebzException {
		innerFileSystem.inflate(cacheImpl, file);
	}

	private void dropFileContentAndChildPathnames(String pathname) {

		cacheImpl.dropChildPathnamesHolderFromCache(pathname);
		// TODO drop whole sub-tree (mind possible gaps in cache caused by evictions - maybe configure child pathnames cache to not evict
		// anything ever at all?) - think in which cases this operation should be done

		cacheImpl.dropFileContentHolderFromCache(pathname);
	}

	private void dropPathnameInCaches(String pathname) {
		cacheImpl.dropMetadataFromCache(pathname);
		dropFileContentAndChildPathnames(pathname);
	}

	private void dropPathnameInCachesAndUpdateMetadata(String pathname, WebzMetadata metadata) {

		if (metadata == null) {
			dropPathnameInCaches(pathname);
		} else {
			cacheImpl.putMetadataIntoCache(pathname, metadata);

			dropFileContentAndChildPathnames(pathname);
		}
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

		ParentChildrenMetadata parentChildrenMetadata = new ParentChildrenMetadata();
		parentChildrenMetadata.folderHash = childPathnamesHolder.folderHash;

		parentChildrenMetadata.parentMetadata = cacheImpl.fetchMetadata(parentPathname);
		parentChildrenMetadata.childPathnamesAndMetadata = cacheImpl.fetchMetadata(childPathnamesHolder.childPathnames);

		return parentChildrenMetadata;
	}

	@Override
	public Map<String, WebzMetadata> getChildPathnamesAndMetadata(String parentPathname) {
		ChildPathnamesHolder childPathnamesHolder = cacheImpl.fetchChildPathnamesHolder(parentPathname);
		return childPathnamesHolder == null ? null : cacheImpl.fetchMetadata(childPathnamesHolder.childPathnames);
	}

	@Override
	public Collection<String> getChildPathnames(String parentPathname) {
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
			return innerFileSystem.getFileDownloader(pathname);
		} else {

			if (payloadHolder.content.size() > filePayloadSizeThreshold) {
				if (LOG.isWarnEnabled()) {
					LOG.warn(WebzUtils.formatFileSystemMessage("payload for '" + pathname
							+ "' was cached even though it is bigger than the threshold - payload threshold (bytes): "
							+ filePayloadSizeThreshold + "; actual file size (bytes): " + payloadHolder.content.size()
							+ " - removing it from cache...", this));
				}
				cacheImpl.putFileContentHolderIntoCache(pathname, new FileContentHolder()); // putting empty content holder instead...
				// TODO think if all caches should be dropped in such a case
			}

			WebzMetadata.FileSpecific fileSpecific = fetchFileSpecific(pathname);
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
	public WebzMetadata createFolder(String pathname) throws IOException, WebzException {

		WebzMetadata metadata = innerFileSystem.createFolder(pathname);

		dropPathnameInCachesAndUpdateMetadata(pathname, metadata);
		return metadata;
	}

	@Override
	public WebzMetadata.FileSpecific uploadFile(String pathname, InputStream content, long numBytes) throws IOException, WebzException {

		WebzMetadata.FileSpecific fileSpecific = innerFileSystem.uploadFile(pathname, content, numBytes);

		dropPathnameInCachesAndUpdateMetadata(pathname, fileSpecific);
		return fileSpecific;
	}

	@Override
	public WebzMetadata.FileSpecific uploadFile(String pathname, InputStream content) throws IOException, WebzException {

		WebzMetadata.FileSpecific fileSpecific = innerFileSystem.uploadFile(pathname, content);

		dropPathnameInCachesAndUpdateMetadata(pathname, fileSpecific);
		return fileSpecific;
	}

	@Override
	public WebzMetadata move(String srcPathname, String destPathname) throws IOException, WebzException {

		WebzMetadata metadata = innerFileSystem.move(srcPathname, destPathname);

		dropPathnameInCaches(srcPathname);
		dropPathnameInCachesAndUpdateMetadata(destPathname, metadata);
		return metadata;
	}

	@Override
	public WebzMetadata copy(String srcPathname, String destPathname) throws IOException, WebzException {

		WebzMetadata metadata = innerFileSystem.copy(srcPathname, destPathname);

		dropPathnameInCachesAndUpdateMetadata(destPathname, metadata);
		return metadata;
	}

	@Override
	public void delete(String pathname) throws IOException, WebzException {

		innerFileSystem.delete(pathname);

		dropPathnameInCaches(pathname);
	}

}
