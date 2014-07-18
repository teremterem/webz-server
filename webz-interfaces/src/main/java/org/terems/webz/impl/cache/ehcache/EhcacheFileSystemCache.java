package org.terems.webz.impl.cache.ehcache;

import java.io.ByteArrayOutputStream;
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
import org.terems.webz.impl.cache.BaseFileSystemCache;
import org.terems.webz.impl.cache.ChildPathNamesHolder;
import org.terems.webz.impl.cache.FilePayloadHolder;
import org.terems.webz.internal.ParentChildrenMetadata;
import org.terems.webz.internal.WebzFileDownloader;
import org.terems.webz.internal.WebzFileSystem;

//TODO cached versions of metadata should not contain whole DbxEntry objects - only needed data (also don't forget to drop payloads that are too big)
public class EhcacheFileSystemCache extends BaseFileSystemCache {

	private static Logger LOG = LoggerFactory.getLogger(EhcacheFileSystemCache.class);

	private Ehcache metadataCache;
	private Ehcache childPathNamesCache;
	private Ehcache payloadsCache;

	// TODO get rid of this holder - temporary solution for not found files (for cases like missed index.html etc.)
	@Deprecated
	public static class MetadataHolder {
		public WebzFileMetadata metadata;
	}

	public EhcacheFileSystemCache(WebzFileSystem fileSystem) {
		super(fileSystem);
	}

	public EhcacheFileSystemCache(WebzFileSystem fileSystem, int filePayloadSizeThreshold) {
		super(fileSystem, filePayloadSizeThreshold);
	}

	@Override
	protected String getCacheTypeName() {
		return "Ehcaches";
	}

	@Override
	protected WebzFileMetadata fetchMetadata(String pathName) {
		MetadataHolder metadataHolder = fetchFromSelfPopulatingCache(metadataCache, pathName);
		return metadataHolder.metadata;
	}

	@Override
	protected Map<String, WebzFileMetadata> fetchMetadata(Collection<String> pathNames) {

		Map<Object, Element> elementsMap = metadataCache.getAll(pathNames);
		Map<String, WebzFileMetadata> pathNamesAndMetadata = new LinkedHashMap<>(pathNames.size());

		for (Map.Entry<Object, Element> entry : elementsMap.entrySet()) {

			String pathName = assertPathName(entry.getKey());
			Element element = entry.getValue();

			if (element == null) {
				if (LOG.isWarnEnabled()) {
					traceFSMessage(LOG,
							"metadata bulk-fetch bottleneck: separate file system call is being made in order to fetch metadata specifically for '"
									+ pathName + "'");
				}
				element = metadataCache.get(pathName);
				// SelfPopulatingCache will always return non-null element...
			}

			pathNamesAndMetadata.put(pathName, (WebzFileMetadata) element.getObjectValue());
		}

		return pathNamesAndMetadata;
	}

	@Override
	protected ChildPathNamesHolder fetchChildPathNamesHolder(String parentPathName) {
		return fetchFromSelfPopulatingCache(childPathNamesCache, parentPathName);
	}

	@Override
	protected FilePayloadHolder fetchFilePayloadHolder(String pathName) {
		return fetchFromSelfPopulatingCache(payloadsCache, pathName);
	}

	@Override
	protected void putMetadataIntoCache(String pathName, WebzFileMetadata metadata) {
		MetadataHolder metadataHolder = new MetadataHolder();
		metadataHolder.metadata = metadata;
		metadataCache.put(new Element(pathName, metadataHolder));
	}

	@Override
	protected void dropMetadataFromCache(String pathName) {
		metadataCache.remove(pathName);
	}

	@Override
	protected void dropChildPathNamesHolderFromCache(String parentPathName) {
		childPathNamesCache.remove(parentPathName);
	}

	@Override
	protected void dropFilePayloadHolderFromCache(String pathName) {
		payloadsCache.remove(pathName);
	}

	@Override
	protected void initSelfPopulatingCaches() {
		// TODO allow ehcache.xml to be placed in dropbox folder (a background phantom thread should periodically check if it
		// was updated)
		Configuration configuration = ConfigurationFactory.parseConfiguration();
		configuration.setName("ehcache-manager-for-" + fileSystem.getFileSystemUniqueId());

		CacheManager cacheManager = CacheManager.create(configuration);

		Ehcache metadataCacheInternal = cacheManager.addCacheIfAbsent("metadata-cache");
		metadataCache = new SelfPopulatingCache(metadataCacheInternal, new CacheEntryFactory() {

			@Override
			public Object createEntry(Object pathName) throws Exception {
				if (LOG.isTraceEnabled()) {
					traceFSMessage(LOG, "fetching METADATA for '" + pathName + "'");
				}

				MetadataHolder metadataHolder = new MetadataHolder();
				metadataHolder.metadata = fileSystem.getMetadata(assertPathName(pathName));
				return metadataHolder;
			}
		});
		cacheManager.replaceCacheWithDecoratedCache(metadataCacheInternal, metadataCache);

		Ehcache childPathNamesCacheInternal = cacheManager.addCacheIfAbsent("child-path-names-cache");
		childPathNamesCache = new SelfPopulatingCache(childPathNamesCacheInternal, new CacheEntryFactory() {

			@Override
			public Object createEntry(Object pathName) throws Exception {
				if (LOG.isTraceEnabled()) {
					traceFSMessage(LOG, "fetching METADATA WITH CHILDREN for '" + pathName + "'");
				}

				String stringPathName = assertPathName(pathName);
				ParentChildrenMetadata parentChildrenMetadata = fileSystem.getParentChildrenMetadata(stringPathName);

				if (parentChildrenMetadata == null) {
					return null;
				}

				putMetadataIntoCache(stringPathName, parentChildrenMetadata.parentMetadata);
				// TODO drop all file system caches if new metadata is different from old metadata for this pathName ?

				ChildPathNamesHolder childPathNamesHolder = new ChildPathNamesHolder();
				childPathNamesHolder.folderHash = parentChildrenMetadata.folderHash;

				if (parentChildrenMetadata.childPathNamesAndMetadata != null) {
					childPathNamesHolder.childPathNames = new ArrayList<>(parentChildrenMetadata.childPathNamesAndMetadata
							.size());
					for (Map.Entry<String, WebzFileMetadata> entry : parentChildrenMetadata.childPathNamesAndMetadata
							.entrySet()) {
						childPathNamesHolder.childPathNames.add(entry.getKey());
						putMetadataIntoCache(entry.getKey(), entry.getValue());
						// TODO drop all file system caches if new metadata is different from old metadata for this pathName ?
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
				String stringPathName = assertPathName(pathName);
				WebzFileDownloader downloader = fileSystem.getFileContentDownloader(stringPathName);
				if (downloader == null || downloader.metadata == null) {
					return null;
				}

				if (LOG.isTraceEnabled()) {
					traceFSMessage(LOG, "fetching PAYLOAD for '" + pathName + "'");
				}

				putMetadataIntoCache(stringPathName, downloader.metadata);
				// TODO drop all file system caches if new metadata is different from old metadata for this pathName ?

				FilePayloadHolder payloadHolder = new FilePayloadHolder();

				WebzFileMetadata.FileSpecific fileSpecific = downloader.metadata.getFileSpecific();
				if (fileSpecific == null) {
					throw new WebzException("'" + pathName + "' is not a file");
				}

				if (fileSpecific.getNumberOfBytes() <= filePayloadSizeThreshold) {
					payloadHolder.payload = new ByteArrayOutputStream((int) fileSpecific.getNumberOfBytes());
					downloader.fileContentToOutputStream(payloadHolder.payload);
				} else if (LOG.isTraceEnabled()) {
					traceFSMessage(LOG, "PAYLOAD for '" + pathName
							+ "' is TOO BIG and thus cannot be cached - payload threshold (bytes): " + filePayloadSizeThreshold
							+ "; actual file size (bytes): " + fileSpecific.getNumberOfBytes());
				}

				return payloadHolder;
			}
		});
		cacheManager.replaceCacheWithDecoratedCache(payloadsCacheInternal, payloadsCache);

		if (LOG.isTraceEnabled()) {
			traceCacheManagers();
		}
	}

	private String assertPathName(Object pathName) {
		if (!(pathName instanceof String)) {
			throw new ClassCastException("pathName should be of type String");
		}
		return (String) pathName;
	}

	@SuppressWarnings("unchecked")
	private <T> T fetchFromSelfPopulatingCache(Ehcache cache, Object key) {
		Element element = cache.get(key);
		// SelfPopulatingCache will always return non-null element...
		return (T) element.getObjectValue();
	}

	private void traceCacheManagers() {
		StringBuffer logMessage = new StringBuffer(
				"\n\n****************************************************************************************************"
						+ "\n***  Ehcache - cache managers and caches:"
						+ "\n****************************************************************************************************");
		for (CacheManager cacheManager : CacheManager.ALL_CACHE_MANAGERS) {
			logMessage.append("\n***");
			logMessage.append("\n***  " + cacheManager.getName());
			logMessage.append("\n***");
			for (String cacheName : cacheManager.getCacheNames()) {
				logMessage.append("\n***  " + cacheName);
			}
			logMessage.append("\n***");
		}
		logMessage
				.append("\n****************************************************************************************************\n");
		LOG.trace(logMessage.toString());
	}

}
