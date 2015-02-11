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

package org.terems.webz.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzMetadata;
import org.terems.webz.WebzProperties;
import org.terems.webz.util.WebzUtils;

public class MimetypesConfig extends WebzConfigObject {

	private Map<String, String> mimetypes = new HashMap<String, String>();

	@Override
	public void init(WebzFile configFolder) throws WebzException {

		WebzFile file = configFolder.getDescendant(WebzProperties.MIMETYPES_PROPERTIES_FILE);
		Properties mimetypesProperties = WebzUtils.loadProperties(file, false);

		for (Map.Entry<Object, Object> entry : mimetypesProperties.entrySet()) {
			mimetypes.put(WebzUtils.toLowerCaseEng(WebzUtils.assertString(entry.getKey())), WebzUtils.assertString(entry.getValue()));
		}
	}

	public String getMimetype(WebzMetadata metadata, String defaultMimetype) throws WebzException {

		String fileExtension = WebzUtils.getFileExtension(metadata);

		if (fileExtension == null) {
			return defaultMimetype;
		}
		String mimetype = mimetypes.get(WebzUtils.toLowerCaseEng(fileExtension));

		return mimetype == null ? defaultMimetype : mimetype;
	}

}
