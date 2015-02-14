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

package org.terems.webz.impl;

import org.terems.webz.WebzDefaults;
import org.terems.webz.WebzException;
import org.terems.webz.WebzProperties;
import org.terems.webz.base.BaseWebzDestroyable;
import org.terems.webz.impl.cache.FileSystemCacheWrapper;
import org.terems.webz.internals.WebzFileFactory;
import org.terems.webz.internals.WebzFileSystem;
import org.terems.webz.internals.WebzFileSystemImpl;
import org.terems.webz.internals.WebzFileSystemOperations;
import org.terems.webz.internals.WebzFileSystemStructure;
import org.terems.webz.internals.WebzObjectFactory;
import org.terems.webz.internals.WebzPathNormalizer;

public class GenericWebzFileSystem extends BaseWebzDestroyable implements WebzFileSystem {

	private String uniqueId;
	private WebzFileFactory fileFactory;
	private WebzPathNormalizer pathNormalizer;
	private WebzFileSystemStructure structure;
	private WebzFileSystemOperations operations;

	@Override
	public GenericWebzFileSystem init(WebzProperties properties, WebzObjectFactory factory) throws WebzException {

		fileFactory = factory.newDestroyable(DefaultWebzFileFactory.class);
		fileFactory.init(this, properties);

		pathNormalizer = factory.newDestroyable(ForwardSlashNormalizer.class);
		pathNormalizer.init(properties);

		WebzFileSystemImpl fsImpl = ((WebzFileSystemImpl) factory.newDestroyable(properties.get(WebzProperties.WEBZ_FS_IMPL_CLASS_PROPERTY,
				WebzDefaults.FS_IMPL_CLASS)));
		fsImpl.init(pathNormalizer, properties, factory);
		fsImpl = WebzFileSystemImplTracer.wrapIfApplicable(fsImpl);

		boolean cacheEnabled = Boolean.valueOf(properties.get(WebzProperties.FS_CACHE_ENABLED_PROPERTY,
				String.valueOf(WebzDefaults.FS_CACHE_ENABLED)));
		if (cacheEnabled) {

			FileSystemCacheWrapper fsCacheWrapper = factory.newDestroyable(FileSystemCacheWrapper.class);
			fsCacheWrapper.init(fsImpl, pathNormalizer, properties, factory);

			fsImpl = fsCacheWrapper;
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
