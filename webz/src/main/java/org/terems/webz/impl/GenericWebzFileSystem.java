package org.terems.webz.impl;

import org.terems.webz.WebzDefaults;
import org.terems.webz.WebzException;
import org.terems.webz.WebzProperties;
import org.terems.webz.base.BaseWebzPropertiesInitable;
import org.terems.webz.impl.cache.CachedFileSystem;
import org.terems.webz.internals.WebzFileFactory;
import org.terems.webz.internals.WebzFileSystem;
import org.terems.webz.internals.WebzFileSystemCache;
import org.terems.webz.internals.WebzFileSystemImpl;
import org.terems.webz.internals.WebzFileSystemOperations;
import org.terems.webz.internals.WebzFileSystemStructure;
import org.terems.webz.internals.WebzPathNormalizer;
import org.terems.webz.internals.base.LowerCaseNormalizer;

public class GenericWebzFileSystem extends BaseWebzPropertiesInitable implements WebzFileSystem {

	private String uniqueId;
	private WebzFileFactory fileFactory;
	private WebzPathNormalizer pathNormalizer;
	private WebzFileSystemStructure structure;
	private WebzFileSystemOperations operations;

	WebzDestroyableFactory factory;

	@Override
	protected void init() throws WebzException {

		WebzProperties properties = getProperties();

		fileFactory = new DefaultWebzFileFactory(this);
		pathNormalizer = new LowerCaseNormalizer();

		WebzFileSystemImpl fileSystemImpl = factory.newDestroyable(properties.get(WebzProperties.WEBZ_FS_IMPL_CLASS_PROPERTY,
				WebzDefaults.FS_IMPL_CLASS));

		fileSystemImpl.setPathNormalizer(pathNormalizer);
		fileSystemImpl.init(properties);

		boolean cacheEnabled = Boolean.valueOf(properties.get(WebzProperties.FS_CACHE_ENABLED_PROPERTY,
				String.valueOf(WebzDefaults.FS_CACHE_ENABLED)));
		if (cacheEnabled) {

			WebzFileSystemCache fsCache = factory.newDestroyable(properties.get(WebzProperties.FS_CACHE_IMPL_CLASS_PROPERTY,
					WebzDefaults.FS_CACHE_IMPL_CLASS));
			int payloadThreshold = Integer.valueOf(properties.get(WebzProperties.FS_CACHE_PAYLOAD_THRESHOLD_BYTES_PROPERTY,
					String.valueOf(WebzDefaults.FS_CACHE_PAYLOAD_THRESHOLD_BYTES)));

			// TODO finish WebzFileSystem refactoring

			fileSystemImpl = new CachedFileSystem(fileSystemImpl, fsCache, payloadThreshold);

			fileSystemImpl.setPathNormalizer(pathNormalizer);
			fileSystemImpl.init(properties);
		}

		structure = fileSystemImpl;
		operations = fileSystemImpl;

		uniqueId = fileSystemImpl.getUniqueId();
	}

	@Override
	public String getUniqueId() {
		return uniqueId;
	}

	@Override
	public WebzFileFactory getFileFactory() {
		return fileFactory;
	}

	@Override
	public WebzPathNormalizer getPathNormalizer() {
		return pathNormalizer;
	}

	@Override
	public WebzFileSystemStructure getStructure() {
		return structure;
	}

	@Override
	public WebzFileSystemOperations getOperations() {
		return operations;
	}

}
