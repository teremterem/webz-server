/*
 * WebZ Server is a server that can serve web pages from various sources.
 * Copyright (C) 2013-2015  Oleksandr Tereschenko <http://ww.webz.bz/>
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

import java.util.Properties;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzProperties;
import org.terems.webz.util.WebzUtils;

public class StatusCodesConfig extends WebzConfigObject {

	private String pathTo404file;
	private String pathTo500file;

	@Override
	public void init(WebzFile configFolder) throws WebzException {

		WebzFile file = configFolder.getDescendant(WebzProperties.STATUS_CODES_PROPERTIES_FILE);
		Properties properties = WebzUtils.loadProperties(file, false);

		pathTo404file = properties.getProperty(WebzProperties.PATH_TO_404_FILE_PROPERTY);
		pathTo500file = properties.getProperty(WebzProperties.PATH_TO_500_FILE_PROPERTY);
	}

	public String getPathTo404file() {
		return pathTo404file;
	}

	public String getPathTo500file() {
		return pathTo500file;
	}

}
