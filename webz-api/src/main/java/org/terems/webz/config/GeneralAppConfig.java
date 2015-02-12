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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;

import org.terems.webz.WebzDefaults;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzProperties;
import org.terems.webz.util.WebzUtils;

/** TODO !!! describe !!! **/
public class GeneralAppConfig extends WebzConfigObject {

	private String appDisplayName;
	private String defaultMimetype;
	private String defaultEncoding;

	private Collection<String> welcomeExtensionsLowerCased;
	private Collection<String> welcomeFilenamesLowerCased;

	/** TODO !!! describe !!! **/
	public String getAppDisplayName() {
		return appDisplayName;
	}

	/** TODO !!! describe !!! **/
	public String getDefaultMimetype() {
		return defaultMimetype;
	}

	/** TODO !!! describe !!! **/
	public String getDefaultEncoding() {
		return defaultEncoding;
	}

	/** TODO !!! describe !!! **/
	public Collection<String> getWelcomeExtensionsLowerCased() {
		return welcomeExtensionsLowerCased;
	}

	/** TODO !!! describe !!! **/
	public Collection<String> getWelcomeFilenamesLowerCased() {
		return welcomeFilenamesLowerCased;
	}

	@Override
	public void init(WebzFile configFolder) throws WebzException {

		WebzFile file = configFolder.getDescendant(WebzProperties.GENERAL_PROPERTIES_FILE);
		Properties properties = WebzUtils.loadProperties(file, false);

		appDisplayName = properties.getProperty(WebzProperties.APP_DISPLAY_NAME_PROPERTY);
		defaultMimetype = properties.getProperty(WebzProperties.DEFAULT_MIMETYPE_PROPERTY, WebzDefaults.MIMETYPE);
		defaultEncoding = properties.getProperty(WebzProperties.DEFAULT_ENCODING_PROPERTY, WebzDefaults.ENCODING);

		welcomeExtensionsLowerCased = populateLowerCasedWelcomeItems(properties.getProperty(WebzProperties.WELCOME_EXTENSIONS_PROPERTY,
				WebzDefaults.WELCOME_EXTENSIONS_LIST));
		welcomeFilenamesLowerCased = populateLowerCasedWelcomeItems(properties.getProperty(WebzProperties.WELCOME_FILENAMES_PROPERTY,
				WebzDefaults.WELCOME_FILENAMES_LIST));
	}

	private Collection<String> populateLowerCasedWelcomeItems(String welcomeItemsCsv) {

		Collection<String> result = new LinkedHashSet<String>();

		for (String item : WebzUtils.parseCsv(welcomeItemsCsv)) {
			result.add(WebzUtils.toLowerCaseEng(item));
		}
		return result;
	}

}
