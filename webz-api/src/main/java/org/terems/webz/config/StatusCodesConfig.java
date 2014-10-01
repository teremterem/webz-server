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
		Properties properties = WebzUtils.loadProperties(file);

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
