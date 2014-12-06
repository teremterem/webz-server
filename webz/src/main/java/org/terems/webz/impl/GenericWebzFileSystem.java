package org.terems.webz.impl;

import org.terems.webz.WebzDefaults;
import org.terems.webz.WebzException;
import org.terems.webz.WebzProperties;
import org.terems.webz.base.BaseWebzDestroyable;
import org.terems.webz.impl.cache.CachedFileSystem;
import org.terems.webz.internals.WebzFileFactory;
import org.terems.webz.internals.WebzFileSystem;
import org.terems.webz.internals.WebzFileSystemImpl;
import org.terems.webz.internals.WebzFileSystemOperations;
import org.terems.webz.internals.WebzFileSystemStructure;
import org.terems.webz.internals.WebzObjectFactory;
import org.terems.webz.internals.WebzPathNormalizer;
import org.terems.webz.internals.base.ForwardSlashNormalizer;

public class GenericWebzFileSystem extends BaseWebzDestroyable implements WebzFileSystem {

	private String uniqueId;
	private WebzFileFactory fileFactory;
	private WebzPathNormalizer pathNormalizer;
	private WebzFileSystemStructure structure;
	private WebzFileSystemOperations operations;

	@Override
	public GenericWebzFileSystem init(WebzObjectFactory factory, WebzProperties properties) throws WebzException {

		fileFactory = factory.newDestroyable(DefaultWebzFileFactory.class);
		fileFactory.init(this, properties);

		pathNormalizer = factory.newDestroyable(ForwardSlashNormalizer.class);
		pathNormalizer.init(properties);

		WebzFileSystemImpl fsImpl = ((WebzFileSystemImpl) factory.newDestroyable(properties.get(WebzProperties.WEBZ_FS_IMPL_CLASS_PROPERTY,
				WebzDefaults.FS_IMPL_CLASS)));
		fsImpl.init(pathNormalizer, properties);
		fsImpl = WebzFileSystemImplTracer.wrapIfApplicable(fsImpl);

		boolean cacheEnabled = Boolean.valueOf(properties.get(WebzProperties.FS_CACHE_ENABLED_PROPERTY,
				String.valueOf(WebzDefaults.FS_CACHE_ENABLED)));
		if (cacheEnabled) {

			CachedFileSystem cachedFileSystem = factory.newDestroyable(CachedFileSystem.class);
			cachedFileSystem.init(fsImpl, pathNormalizer, properties, factory);

			fsImpl = cachedFileSystem;
		}
		structure = fsImpl;
		operations = fsImpl;

		uniqueId = fsImpl.getUniqueId();
		return this;
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
