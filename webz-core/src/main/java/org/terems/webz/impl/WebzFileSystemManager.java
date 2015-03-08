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
import org.terems.webz.base.BaseWebzDestroyable;
import org.terems.webz.internals.WebzFileSystem;
import org.terems.webz.internals.WebzObjectFactory;
import org.terems.webz.internals.WebzPathNormalizer;

public class WebzFileSystemManager extends BaseWebzDestroyable {

	public static WebzFileSystemManager getManager(WebzObjectFactory factory) throws WebzException {

		WebzFileSystemManager manager = factory.getDestroyableSingleton(WebzFileSystemManager.class);
		manager.factory = factory;

		return manager;
	}

	private static final WebzPathNormalizer DEFAULT_PATH_NORMALIZER = new ForwardSlashNormalizer();

	private WebzObjectFactory factory;

	public WebzFileSystem createFileSystem(WebzProperties webzProperties) throws WebzException {
		return factory.newDestroyable(GenericWebzFileSystem.class).init(DEFAULT_PATH_NORMALIZER, webzProperties, factory);
	}

	public WebzFileSystem createSpaSiteFileSystem(WebzFileSystem spaFileSystem, WebzFileSystem siteFileSystem,
			WebzProperties webzProperties) throws WebzException {
		return factory.newDestroyable(SpaSiteWebzFileSystem.class).init(DEFAULT_PATH_NORMALIZER, spaFileSystem, siteFileSystem,
				webzProperties, factory);
	}

}
