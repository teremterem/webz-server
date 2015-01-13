package org.terems.webz.impl.cache;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzDefaults;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzMetadata;
import org.terems.webz.WebzProperties;
import org.terems.webz.WebzWriteException;
import org.terems.webz.internals.ParentChildrenMetadata;
import org.terems.webz.internals.WebzFileSystemCache;
import org.terems.webz.internals.WebzFileSystemImpl;
import org.terems.webz.internals.WebzObjectFactory;
import org.terems.webz.internals.WebzPathNormalizer;
import org.terems.webz.internals.base.BaseWebzFileSystemImpl;
import org.terems.webz.internals.cache.ChildPathnamesHolder;
import org.terems.webz.internals.cache.FileContentHolder;
import org.terems.webz.internals.cache.WebzByteArrayInputStream;
import org.terems.webz.util.WebzUtils;

// TODO background thread should periodically check certain number of pathnames against Dropbox to drop the whole FS cache if necessary
// TODO also do similar check when some metadata is being fetched as a "side-effect" in cache implementations to drop the whole FS cache if necessary
public class CachedFileSystem extends BaseWebzFileSystemImpl {

	private static final Logger LOG = LoggerFactory.getLogger(CachedFileSystem.class);

	private WebzFileSystemImpl fileSystemImpl;
	private WebzFileSystemCache cacheImpl;

	private int filePayloadSizeThreshold;

	public CachedFileSystem init(WebzFileSystemImpl fileSystemImpl, WebzPathNormalizer pathNormalizer, WebzProperties properties,
			WebzObjectFactory factory) throws WebzException {

		// TODO additional mode #1: payload cache disabled completely
		// TODO additional mode #2: payload cache works for any payload sizes without the threshold ?

		if (fileSystemImpl instanceof CachedFileSystem) {
			throw new IllegalArgumentException(
					"an instance of BaseFileSystemCache should not be wrapped with another instance of BaseFileSystemCache - an attempt was made to wrap "
							+ fileSystemImpl.getUniqueId() + " with an instance of " + getClass());
		}
		this.fileSystemImpl = fileSystemImpl;

		filePayloadSizeThreshold = Integer.valueOf(properties.get(WebzProperties.FS_CACHE_PAYLOAD_THRESHOLD_BYTES_PROPERTY,
				String.valueOf(WebzDefaults.FS_CACHE_PAYLOAD_THRESHOLD_BYTES)));
		cacheImpl = ((WebzFileSystemCache) factory.newDestroyable(properties.get(WebzProperties.FS_CACHE_IMPL_CLASS_PROPERTY,
				WebzDefaults.FS_CACHE_IMPL_CLASS))).init(fileSystemImpl, filePayloadSizeThreshold);

		this.uniqueId = cacheImpl.getCacheTypeName() + "-for-" + fileSystemImpl.getUniqueId();

		if (LOG.isInfoEnabled()) {
			LOG.info("'" + this.uniqueId + "' file system cache was created to wrap '" + fileSystemImpl.getUniqueId() + "'");
		}
		super.init(pathNormalizer, properties);
		return this;
	}

	@Override
	public void inflate(WebzFile file) throws IOException, WebzException {
		fileSystemImpl.inflate(cacheImpl, file);
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

			final WebzByteArrayInputStream webzIn = payloadHolder.content.createInputStream();
			return new WebzFileDownloader(fileSpecific, webzIn) {

				@Override
				protected long copyContent(OutputStream out) throws WebzWriteException {
					return webzIn.writeAvailableToOutputStream(out);
				}
			};
		}
	}

	@Override
	public WebzMetadata createFolder(String pathname) throws IOException, WebzException {

		WebzMetadata metadata = fileSystemImpl.createFolder(pathname);

		dropPathnameInCachesAndUpdateMetadata(pathname, metadata);
		return metadata;
	}

	@Override
	public WebzMetadata.FileSpecific uploadFile(String pathname, InputStream content, long numBytes) throws IOException, WebzException {

		WebzMetadata.FileSpecific fileSpecific = fileSystemImpl.uploadFile(pathname, content, numBytes);

		dropPathnameInCachesAndUpdateMetadata(pathname, fileSpecific);
		return fileSpecific;
	}

	@Override
	public WebzMetadata.FileSpecific uploadFile(String pathname, InputStream content) throws IOException, WebzException {

		WebzMetadata.FileSpecific fileSpecific = fileSystemImpl.uploadFile(pathname, content);

		dropPathnameInCachesAndUpdateMetadata(pathname, fileSpecific);
		return fileSpecific;
	}

	@Override
	public WebzMetadata move(String srcPathname, String destPathname) throws IOException, WebzException {

		WebzMetadata metadata = fileSystemImpl.move(srcPathname, destPathname);

		dropPathnameInCaches(srcPathname);
		dropPathnameInCachesAndUpdateMetadata(destPathname, metadata);
		return metadata;
	}

	@Override
	public WebzMetadata copy(String srcPathname, String destPathname) throws IOException, WebzException {

		WebzMetadata metadata = fileSystemImpl.copy(srcPathname, destPathname);

		dropPathnameInCachesAndUpdateMetadata(destPathname, metadata);
		return metadata;
	}

	@Override
	public void delete(String pathname) throws IOException, WebzException {

		fileSystemImpl.delete(pathname);

		dropPathnameInCaches(pathname);
	}

}
