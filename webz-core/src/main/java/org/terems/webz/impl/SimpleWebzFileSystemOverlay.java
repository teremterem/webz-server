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

import org.terems.webz.WebzException;
import org.terems.webz.WebzProperties;
import org.terems.webz.internals.WebzFileSystem;
import org.terems.webz.internals.WebzObjectFactory;
import org.terems.webz.internals.WebzPathNormalizer;
import org.terems.webz.internals.base.BaseWebzFileSystem;

public class SimpleWebzFileSystemOverlay extends BaseWebzFileSystem {

	private WebzFileSystem primaryFileSystem;
	private WebzFileSystem secondaryFileSystem;

	public SimpleWebzFileSystemOverlay init(WebzPathNormalizer pathNormalizer, WebzFileSystem primaryFileSystem, String primaryOrigin,
			WebzFileSystem secondaryFileSystem, String secondaryOrigin, WebzProperties properties, WebzObjectFactory factory)
			throws WebzException {

		this.pathNormalizer = pathNormalizer;
		this.primaryFileSystem = primaryFileSystem;
		this.secondaryFileSystem = secondaryFileSystem;

		fileFactory = factory.newDestroyable(DefaultWebzFileFactory.class);
		fileFactory.init(this, properties);

		impl = new SimpleFileSystemOverlayImpl(primaryFileSystem.getImpl(), primaryOrigin, secondaryFileSystem.getImpl(), secondaryOrigin);
		impl.init(pathNormalizer, properties, factory);

		uniqueId = impl.getUniqueId();
		return this;
	}

	@Override
	public void setDefaultEncoding(String defaultEncoding) {

		super.setDefaultEncoding(defaultEncoding);
		primaryFileSystem.setDefaultEncoding(defaultEncoding);
		secondaryFileSystem.setDefaultEncoding(defaultEncoding);
	}

}
