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
import org.terems.webz.impl.cache.FileSystemCacheWrapper;
import org.terems.webz.internals.WebzFileSystemImpl;
import org.terems.webz.internals.WebzObjectFactory;
import org.terems.webz.internals.WebzPathNormalizer;
import org.terems.webz.internals.base.BaseWebzFileSystem;

public class GenericWebzFileSystem extends BaseWebzFileSystem {

	public GenericWebzFileSystem init(WebzPathNormalizer pathNormalizer, WebzProperties properties, WebzObjectFactory factory)
			throws WebzException {

		this.pathNormalizer = pathNormalizer;

		fileFactory = factory.newDestroyable(DefaultWebzFileFactory.class);
		fileFactory.init(this, properties);

		impl = ((WebzFileSystemImpl) factory.newDestroyable(properties.get(WebzProperties.WEBZ_FS_IMPL_CLASS_PROPERTY,
				WebzDefaults.FS_IMPL_CLASS)));
		impl.init(pathNormalizer, properties, factory);

		impl = WebzFileSystemImplTracer.wrapIfApplicable(impl);

		boolean cacheEnabled = Boolean.valueOf(properties.get(WebzProperties.FS_CACHE_ENABLED_PROPERTY,
				String.valueOf(WebzDefaults.FS_CACHE_ENABLED)));
		if (cacheEnabled) {

			FileSystemCacheWrapper fsCacheWrapper = factory.newDestroyable(FileSystemCacheWrapper.class);
			fsCacheWrapper.init(impl, pathNormalizer, properties, factory);

			impl = fsCacheWrapper;
		}

		uniqueId = impl.getUniqueId();
		return this;
	}

}
