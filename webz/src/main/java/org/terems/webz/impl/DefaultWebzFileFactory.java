/*
 * WebZ Server is a server that can serve web pages from various sources.
 * Copyright (C) 2013-2015  Oleksandr Tereschenko <http://www.terems.org/>
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
import org.terems.webz.WebzFile;
import org.terems.webz.WebzProperties;
import org.terems.webz.base.BaseWebzDestroyable;
import org.terems.webz.internals.WebzFileFactory;
import org.terems.webz.internals.WebzFileSystem;

public class DefaultWebzFileFactory extends BaseWebzDestroyable implements WebzFileFactory {

	// TODO make this factory a first level cache ? (for the sake of file "inflation" concept)

	private WebzFileSystem fileSystem;
	private boolean useMetadataInflatableFiles;

	@Override
	public DefaultWebzFileFactory init(WebzFileSystem fileSystem, WebzProperties properties) {

		this.fileSystem = fileSystem;
		this.useMetadataInflatableFiles = Boolean.valueOf(properties.get(WebzProperties.USE_METADATA_INFLATABLE_FILES_PROPERTY,
				String.valueOf(WebzDefaults.USE_METADATA_INFLATABLE_FILES)));
		return this;
	}

	@Override
	public WebzFile get(String pathname) {

		WebzFile file = new GenericWebzFile(pathname, fileSystem);
		if (useMetadataInflatableFiles) {
			return new MetadataInflatableWebzFile(file);
		}
		return file;
	}

}
