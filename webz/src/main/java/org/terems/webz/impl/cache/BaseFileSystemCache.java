package org.terems.webz.impl.cache;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.FreshParentChildrenMetadata;
import org.terems.webz.ParentChildrenMetadata;
import org.terems.webz.WebzDefaults;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzFileMetadata;
import org.terems.webz.WebzFileSystem;
import org.terems.webz.WebzFileSystemProxy;

// TODO background thread should periodically check certain number of pathNames against Dropbox to drop the whole FS cache if necessary
// TODO also do similar check when some metadata is being fetched as a "side-effect" in cache implementations to drop the whole FS cache if necessary
/** TODO !!! describe !!! **/
public abstract class BaseFileSystemCache extends WebzFileSystemProxy {

	private static Logger LOG = LoggerFactory.getLogger(BaseFileSystemCache.class);

	protected final int filePayloadSizeThreshold;
	protected final WebzFileSystem fileSystem;

	private final String fileSystemUniqueId;

	/** TODO !!! describe !!! **/
	protected abstract String getCacheTypeName();

	/** TODO !!! describe !!! **/
	protected abstract void putMetadataIntoCache(String pathName, WebzFileMetadata metadata);

	/** TODO !!! describe !!! **/
	protected abstract WebzFileMetadata fetchMetadata(String pathName);

	/** TODO !!! describe !!! **/
	protected abstract Map<String, WebzFileMetadata> fetchMetadata(Collection<String> pathNames);

	/** TODO !!! describe !!! **/
	protected abstract ChildPathNamesHolder fetchChildPathNamesHolder(String parentPathName);

	/** TODO !!! describe !!! **/
	protected abstract FilePayloadHolder fetchFilePayloadHolder(String pathName);

	/** TODO !!! describe !!! **/
	protected abstract void dropMetadataFromCache(String pathName);

	/** TODO !!! describe !!! **/
	protected abstract void dropChildPathNamesHolderFromCache(String parentPathName);

	/** TODO !!! describe !!! **/
	protected abstract void dropFilePayloadHolderFromCache(String pathName);

	/** TODO !!! describe !!! **/
	protected abstract void initSelfPopulatingCaches();

	public BaseFileSystemCache(WebzFileSystem fileSystem) {
		this(fileSystem, WebzDefaults.PAYLOAD_CACHE_THRESHOLD_BYTES);
	}

	public BaseFileSystemCache(WebzFileSystem fileSystem, int filePayloadSizeThreshold) {
		// TODO add two additional modes:
		// 1) payload cache disabled completely
		// 2) payload cache works for any payload sizes without the threshold

		if (fileSystem instanceof BaseFileSystemCache) {
			throw new IllegalArgumentException(
					"an instance of BaseFileSystemCache should not be wrapped with another instance of BaseFileSystemCache - an attempt was made to wrap "
							+ fileSystem.getFileSystemUniqueId() + " with an instance of " + getClass());
		}

		this.filePayloadSizeThreshold = filePayloadSizeThreshold;
		this.fileSystem = fileSystem;
		this.fileSystemUniqueId = getCacheTypeName() + "-for-" + fileSystem.getFileSystemUniqueId();

		initSelfPopulatingCaches();

		LOG.info("'" + this.fileSystemUniqueId + "' file system cache was created to wrap '"
				+ fileSystem.getFileSystemUniqueId() + "'");
	}

	@Override
	protected WebzFileSystem getFileSystem() {
		return fileSystem;
	}

	@Override
	public String getFileSystemUniqueId() {
		return fileSystemUniqueId;
	}

	private void dropFilePayloadAndChildPathNames(String pathName) {

		dropChildPathNamesHolderFromCache(pathName);
		// TODO drop whole sub-tree (mind possible gaps in cache caused by evictions - maybe configure child path names cache to
		// not evict anything ever at all?) - think in which cases this operation should be done

		dropFilePayloadHolderFromCache(pathName);
	}

	private void dropPathNameInCaches(String pathName) {
		dropMetadataFromCache(pathName);
		dropFilePayloadAndChildPathNames(pathName);
	}

	private void dropPathNameInCachesAndUpdateMetadata(String pathName, WebzFileMetadata metadata) {

		if (metadata == null) {
			dropPathNameInCaches(pathName);
		} else {
			putMetadataIntoCache(pathName, metadata);

			dropFilePayloadAndChildPathNames(pathName);
		}
	}

	@Override
	public WebzFileMetadata getMetadata(String pathName) throws IOException, WebzException {
		return fetchMetadata(pathName);
	}

	@Override
	public ParentChildrenMetadata getParentChildrenMetadata(String parentPathName) throws IOException, WebzException {

		ChildPathNamesHolder childPathNamesHolder = fetchChildPathNamesHolder(parentPathName);
		if (childPathNamesHolder == null) {
			return null;
		}

		ParentChildrenMetadata parentChildrenMetadata = new ParentChildrenMetadata();
		parentChildrenMetadata.folderHash = childPathNamesHolder.folderHash;

		parentChildrenMetadata.parentMetadata = fetchMetadata(parentPathName);
		parentChildrenMetadata.childPathNamesAndMetadata = fetchMetadata(childPathNamesHolder.childPathNames);

		return parentChildrenMetadata;
	}

	/**
	 * "Conventional" implementation...
	 **/
	@Override
	public FreshParentChildrenMetadata getParentChildrenMetadataIfChanged(String parentPathName, Object previousFolderHash)
			throws IOException, WebzException {

		ParentChildrenMetadata parentChildrenMetadata = getParentChildrenMetadata(parentPathName);
		if (previousFolderHash != null && parentChildrenMetadata != null
				&& previousFolderHash.equals(parentChildrenMetadata.folderHash)) {
			return null;
		}

		return new FreshParentChildrenMetadata(parentChildrenMetadata);
	}

	@Override
	public Map<String, WebzFileMetadata> getChildPathNamesAndMetadata(String parentPathName) throws IOException, WebzException {
		ChildPathNamesHolder childPathNamesHolder = fetchChildPathNamesHolder(parentPathName);
		return childPathNamesHolder == null ? null : fetchMetadata(childPathNamesHolder.childPathNames);
	}

	@Override
	public Collection<String> getChildPathNames(String parentPathName) throws IOException, WebzException {
		ChildPathNamesHolder childPathNamesHolder = fetchChildPathNamesHolder(parentPathName);
		return childPathNamesHolder == null ? null : childPathNamesHolder.childPathNames;
	}

	private void writeFilePayload(FilePayloadHolder payloadHolder, String pathName, OutputStream out) throws IOException,
			WebzException {
		if (payloadHolder.payload != null) {
			payloadHolder.payload.writeTo(out);
			// TODO drop payload in a thread-safe manner if it is bigger than the threshold + log-warn? about this + think if
			// all caches should be dropped in such a case
		} else {
			if (LOG.isTraceEnabled()) {
				traceFSMessage(LOG, "PAYLOAD for '" + pathName + "' is being fetched without being cached");
			}
			fileSystem.fileContentToOutputStream(pathName, out);
			// TODO check if there is any problem with parallel payload downloading
		}
	}

	private WebzFileMetadata.FileSpecific fetchFileSpecific(final String pathName) throws IOException, WebzException {

		WebzFileMetadata.FileSpecific fileSpecific = fetchMetadata(pathName).getFileSpecific();
		if (fileSpecific == null) {
			throw new WebzException("'" + pathName + "' is not a file");
		}

		return fileSpecific;
	}

	@Override
	public WebzFileMetadata.FileSpecific fileContentToOutputStream(String pathName, OutputStream out) throws IOException,
			WebzException {

		FilePayloadHolder payloadHolder = fetchFilePayloadHolder(pathName);
		if (payloadHolder == null) {
			return null;
		}

		WebzFileMetadata.FileSpecific fileSpecific = fetchFileSpecific(pathName);
		writeFilePayload(payloadHolder, pathName, out);
		return fileSpecific;
	}

	@Override
	public WebzFileDownloader getFileContentDownloader(final String pathName) throws IOException, WebzException {

		final FilePayloadHolder payloadHolder = fetchFilePayloadHolder(pathName);
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
	public WebzFileMetadata createFolder(String pathName) throws IOException, WebzException {

		WebzFileMetadata metadata = super.createFolder(pathName);

		dropPathNameInCachesAndUpdateMetadata(pathName, metadata);
		return metadata;
	}

	@Override
	public WebzFileMetadata.FileSpecific uploadFile(String pathName, byte[] content) throws IOException, WebzException {

		WebzFileMetadata.FileSpecific fileSpecific = super.uploadFile(pathName, content);

		dropPathNameInCachesAndUpdateMetadata(pathName, fileSpecific);
		return fileSpecific;
	}

	@Override
	public WebzFileMetadata move(String srcPathName, String destPathName) throws IOException, WebzException {

		WebzFileMetadata metadata = super.move(srcPathName, destPathName);

		dropPathNameInCaches(srcPathName);
		dropPathNameInCachesAndUpdateMetadata(destPathName, metadata);
		return metadata;
	}

	@Override
	public WebzFileMetadata copy(String srcPathName, String destPathName) throws IOException, WebzException {

		WebzFileMetadata metadata = super.copy(srcPathName, destPathName);

		dropPathNameInCachesAndUpdateMetadata(destPathName, metadata);
		return metadata;
	}

	@Override
	public void delete(String pathName) throws IOException, WebzException {

		super.delete(pathName);

		dropPathNameInCaches(pathName);
	}

	protected void traceFSMessage(Logger log, String message) {
		log.trace(message + " (file system: '" + fileSystem.getFileSystemUniqueId() + "')");
	}

}
