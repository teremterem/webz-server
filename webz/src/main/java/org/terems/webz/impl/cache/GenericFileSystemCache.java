package org.terems.webz.impl.cache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFileMetadata;
import org.terems.webz.impl.base.WebzFileSystemProxy;
import org.terems.webz.internal.FreshParentChildrenMetadata;
import org.terems.webz.internal.ParentChildrenMetadata;
import org.terems.webz.internal.WebzFileDownloader;
import org.terems.webz.internal.WebzFileSystem;
import org.terems.webz.obsolete.WebzConstants;

// TODO background thread should periodically check certain number of pathNames against dropbox
// TODO think if some abstraction can be separated from Ehcache-specific implementation
// TODO cached versions of metadata should not contain whole DbxEntry objects - only needed data
// TODO add trace logs for "cache-miss" activity
public class GenericFileSystemCache extends WebzFileSystemProxy {

	private static Logger LOG = LoggerFactory.getLogger(GenericFileSystemCache.class);

	private final WebzFileSystem fileSystem;
	private int filePayloadSizeThreshold;
	private String fileSystemUniqueId;

	private Ehcache metadataCache;

	private Ehcache childPathNamesCache;

	private Ehcache payloadsCache;

	public GenericFileSystemCache(WebzFileSystem fileSystem, int filePayloadSizeThreshold) {

		if (fileSystem instanceof GenericFileSystemCache) {
			throw new IllegalArgumentException(
					"GenericFileSystemCache should not be wrapped with GenericFileSystemCache. An attempt was made to wrap "
							+ fileSystem._getFileSystemUniqueId() + " with another GenericFileSystemCache instance.");
		}

		this.fileSystem = fileSystem;
		this.filePayloadSizeThreshold = filePayloadSizeThreshold;
		this.fileSystemUniqueId = "generic-cache-for-" + fileSystem._getFileSystemUniqueId();

		// TODO allow ehcache.xml to be placed in dropbox folder (a background phantom thread should periodically check if it
		// was updated)
		Configuration configuration = ConfigurationFactory.parseConfiguration();
		configuration.setName("ehcache-manager-for-" + fileSystem._getFileSystemUniqueId());

		initCaches(CacheManager.create(configuration));
	}

	// TODO get rid of this holder - temporary solution for not found files (for cases like missed index.html etc.)
	@Deprecated
	public static class MetadataHolder {
		public WebzFileMetadata metadata;
	}

	private void initCaches(CacheManager cacheManager) {

		Ehcache metadataCacheInternal = cacheManager.addCacheIfAbsent("metadata-cache");
		metadataCache = new SelfPopulatingCache(metadataCacheInternal, new CacheEntryFactory() {

			@Override
			public Object createEntry(Object pathName) throws Exception {
				// TODO change to trace:
				LOG.error("metadata : " + pathName);

				MetadataHolder metadataHolder = new MetadataHolder();
				metadataHolder.metadata = fileSystem._getMetadata(assertPathName(pathName));
				return metadataHolder;
			}
		});
		cacheManager.replaceCacheWithDecoratedCache(metadataCacheInternal, metadataCache);

		Ehcache childPathNamesCacheInternal = cacheManager.addCacheIfAbsent("child-path-names-cache");
		childPathNamesCache = new SelfPopulatingCache(childPathNamesCacheInternal, new CacheEntryFactory() {

			@Override
			public Object createEntry(Object pathName) throws Exception {
				// TODO change to trace:
				LOG.error("children : " + pathName);

				String stringPathName = assertPathName(pathName);
				ParentChildrenMetadata parentChildrenMetadata = fileSystem._getParentChildrenMetadata(stringPathName);

				if (parentChildrenMetadata == null) {
					return null;
				}

				putMetadataIntoCache(stringPathName, parentChildrenMetadata.parentMetadata);
				// TODO drop all file system caches if new metadata is different than old metadata for this pathName ?

				ChildPathNamesHolder childPathNamesHolder = new ChildPathNamesHolder();
				childPathNamesHolder.folderHash = parentChildrenMetadata.folderHash;

				if (parentChildrenMetadata.childPathNamesAndMetadata != null) {
					childPathNamesHolder.childPathNames = new ArrayList<>(parentChildrenMetadata.childPathNamesAndMetadata
							.size());
					for (Map.Entry<String, WebzFileMetadata> entry : parentChildrenMetadata.childPathNamesAndMetadata
							.entrySet()) {
						childPathNamesHolder.childPathNames.add(entry.getKey());
						putMetadataIntoCache(entry.getKey(), entry.getValue());
						// TODO drop all file system caches if new metadata is different than old metadata for this pathName ?
					}
				}
				return childPathNamesHolder;
			}
		});
		cacheManager.replaceCacheWithDecoratedCache(childPathNamesCacheInternal, childPathNamesCache);

		Ehcache payloadsCacheInternal = cacheManager.addCacheIfAbsent("payloads-cache");
		payloadsCache = new SelfPopulatingCache(payloadsCacheInternal, new CacheEntryFactory() {

			@Override
			public Object createEntry(Object pathName) throws Exception {
				// TODO change to trace:
				LOG.error("payload  : " + pathName);

				String stringPathName = assertPathName(pathName);
				WebzFileDownloader downloader = fileSystem._getFileContentDownloader(stringPathName);
				if (downloader == null || downloader.metadata == null) {
					return null;
				}

				putMetadataIntoCache(stringPathName, downloader.metadata);
				// TODO drop all file system caches if new metadata is different than old metadata for this pathName ?

				FilePayloadHolder payloadHolder = new FilePayloadHolder();

				WebzFileMetadata.FileSpecific fileSpecific = downloader.metadata.getFileSpecific();

				if (fileSpecific.getNumberOfBytes() <= filePayloadSizeThreshold) {
					payloadHolder.payload = new ByteArrayOutputStream((int) fileSpecific.getNumberOfBytes());
					downloader.fileContentToOutputStream(payloadHolder.payload);
				}

				return payloadHolder;
			}
		});
		cacheManager.replaceCacheWithDecoratedCache(payloadsCacheInternal, payloadsCache);
	}

	private String assertPathName(Object pathName) {
		if (!(pathName instanceof String)) {
			throw new ClassCastException("pathName should be of type String");
		}
		return (String) pathName;
	}

	public GenericFileSystemCache(WebzFileSystem fileSystem) {
		this(fileSystem, WebzConstants.DEFAULT_FILE_PAYLOAD_SIZE_THRESHOLD_TO_CACHE);
	}

	@Override
	protected WebzFileSystem getFileSystem() {
		return fileSystem;
	}

	@Override
	public String _getFileSystemUniqueId() {
		return fileSystemUniqueId;
	}

	@SuppressWarnings("unchecked")
	private <T> T fetchFromSelfPopulatingCache(Ehcache cache, Object key) {
		Element element = cache.get(key);
		// SelfPopulatingCache will always return non-null element...
		return (T) element.getObjectValue();
	}

	private WebzFileMetadata fetchMetadata(String pathName) {
		MetadataHolder metadataHolder = fetchFromSelfPopulatingCache(metadataCache, pathName);
		return metadataHolder.metadata;
	}

	private WebzFileMetadata dropPathNameInCachesAndUpdateMetadata(String pathName, WebzFileMetadata metadata) {

		if (metadata == null) {
			dropPathNameInCaches(pathName);
		} else {
			putMetadataIntoCache(pathName, metadata);

			childPathNamesCache.remove(pathName);
			payloadsCache.remove(pathName);
		}

		return metadata;
	}

	private void dropPathNameInCaches(String pathName) {

		metadataCache.remove(pathName);

		childPathNamesCache.remove(pathName);
		payloadsCache.remove(pathName);
	}

	private void putMetadataIntoCache(String pathName, WebzFileMetadata metadata) {
		MetadataHolder metadataHolder = new MetadataHolder();
		metadataHolder.metadata = metadata;
		metadataCache.put(new Element(pathName, metadataHolder));
	}

	private Map<String, WebzFileMetadata> fetchMetadata(Collection<String> pathNames) {

		Map<Object, Element> elementsMap = metadataCache.getAll(pathNames);
		Map<String, WebzFileMetadata> pathNamesAndMetadata = new LinkedHashMap<>(pathNames.size());

		for (Map.Entry<Object, Element> entry : elementsMap.entrySet()) {

			String pathName = assertPathName(entry.getKey());
			Element element = entry.getValue();

			if (element == null) {
				// TODO add trace (or warn?) logs to signal if this method leads to multiple separate requests to dropbox
				element = metadataCache.get(pathName);
				// SelfPopulatingCache will always return non-null element...
			}

			pathNamesAndMetadata.put(pathName, (WebzFileMetadata) element.getObjectValue());
		}

		return pathNamesAndMetadata;
	}

	private ChildPathNamesHolder fetchChildPathNamesHolder(String parentPathName) {
		return fetchFromSelfPopulatingCache(childPathNamesCache, parentPathName);
	}

	private FilePayloadHolder fetchFilePayloadHolder(String pathName) {
		return fetchFromSelfPopulatingCache(payloadsCache, pathName);
	}

	@Override
	public WebzFileMetadata _getMetadata(String pathName) throws IOException, WebzException {
		return fetchMetadata(pathName);
	}

	@Override
	public ParentChildrenMetadata _getParentChildrenMetadata(String parentPathName) throws IOException, WebzException {

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
	public FreshParentChildrenMetadata _getParentChildrenMetadataIfChanged(String parentPathName, Object previousFolderHash)
			throws IOException, WebzException {

		ParentChildrenMetadata parentChildrenMetadata = _getParentChildrenMetadata(parentPathName);
		if (previousFolderHash != null && parentChildrenMetadata != null
				&& previousFolderHash.equals(parentChildrenMetadata.folderHash)) {
			return null;
		}

		return new FreshParentChildrenMetadata(parentChildrenMetadata);
	}

	@Override
	public Map<String, WebzFileMetadata> _getChildPathNamesAndMetadata(String parentPathName) throws IOException, WebzException {
		ChildPathNamesHolder childPathNamesHolder = fetchChildPathNamesHolder(parentPathName);
		return childPathNamesHolder == null ? null : fetchMetadata(childPathNamesHolder.childPathNames);
	}

	@Override
	public Collection<String> _getChildPathNames(String parentPathName) throws IOException, WebzException {
		ChildPathNamesHolder childPathNamesHolder = fetchChildPathNamesHolder(parentPathName);
		return childPathNamesHolder == null ? null : childPathNamesHolder.childPathNames;
	}

	private void writeFilePayload(FilePayloadHolder payloadHolder, String pathName, OutputStream out) throws IOException,
			WebzException {
		if (payloadHolder.payload != null) {
			payloadHolder.payload.writeTo(out);
		} else {
			fileSystem._fileContentToOutputStream(pathName, out);
		}
		// TODO drop payload in synchronous manner if it is bigger than the threshold
	}

	@Override
	public WebzFileMetadata _fileContentToOutputStream(String pathName, OutputStream out) throws IOException, WebzException {
		FilePayloadHolder payloadHolder = fetchFilePayloadHolder(pathName);
		if (payloadHolder == null) {
			return null;
		}

		writeFilePayload(payloadHolder, pathName, out);
		return fetchMetadata(pathName);
	}

	@Override
	public WebzFileDownloader _getFileContentDownloader(final String pathName) throws IOException, WebzException {
		final FilePayloadHolder payloadHolder = fetchFilePayloadHolder(pathName);
		if (payloadHolder == null) {
			return null;
		}

		return new WebzFileDownloader(fetchMetadata(pathName)) {

			@Override
			public void fileContentToOutputStream(OutputStream out) throws IOException, WebzException {
				writeFilePayload(payloadHolder, pathName, out);
			}
		};
	}

	@Override
	public WebzFileMetadata _createFolder(String pathName) throws IOException, WebzException {

		WebzFileMetadata metadata = super._createFolder(pathName);

		return dropPathNameInCachesAndUpdateMetadata(pathName, metadata);
	}

	@Override
	public WebzFileMetadata _uploadFile(String pathName, byte[] content) throws IOException, WebzException {

		WebzFileMetadata metadata = super._uploadFile(pathName, content);

		return dropPathNameInCachesAndUpdateMetadata(pathName, metadata);
	}

	@Override
	public WebzFileMetadata _move(String srcPathName, String destPathName) throws IOException, WebzException {

		WebzFileMetadata metadata = super._move(srcPathName, destPathName);

		dropPathNameInCaches(srcPathName);
		return dropPathNameInCachesAndUpdateMetadata(destPathName, metadata);
	}

	@Override
	public WebzFileMetadata _copy(String srcPathName, String destPathName) throws IOException, WebzException {

		WebzFileMetadata metadata = super._copy(srcPathName, destPathName);

		return dropPathNameInCachesAndUpdateMetadata(destPathName, metadata);
	}

	@Override
	public void _delete(String pathName) throws IOException, WebzException {

		super._delete(pathName);

		dropPathNameInCaches(pathName);
	}

}
