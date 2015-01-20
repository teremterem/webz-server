package org.terems.webz.config;

import java.util.Properties;

import org.terems.webz.WebzDefaults;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzProperties;
import org.terems.webz.util.WebzUtils;

public class GeneralAppConfig extends WebzConfigObject {

	private String appDisplayName;
	private String defaultMimetype;
	private String defaultEncoding;

	private String welcomeExtensionsList;
	private String welcomeFilenamesList;

	@Override
	public void init(WebzFile configFolder) throws WebzException {

		WebzFile file = configFolder.getDescendant(WebzProperties.GENERAL_PROPERTIES_FILE);
		Properties properties = WebzUtils.loadProperties(file);

		// TODO do not fail if properties file is absent (warn ?)

		appDisplayName = properties.getProperty(WebzProperties.APP_DISPLAY_NAME_PROPERTY);
		defaultMimetype = properties.getProperty(WebzProperties.DEFAULT_MIMETYPE_PROPERTY, WebzDefaults.DEFAULT_MIMETYPE);
		defaultEncoding = properties.getProperty(WebzProperties.DEFAULT_ENCODING_PROPERTY, WebzDefaults.DEFAULT_ENCODING);

		welcomeExtensionsList = properties.getProperty(WebzProperties.WELCOME_EXTENSIONS_PROPERTY,
				WebzDefaults.DEFAULT_WELCOME_EXTENSIONS_LIST);
		welcomeFilenamesList = properties.getProperty(WebzProperties.WELCOME_FILENAMES_PROPERTY,
				WebzDefaults.DEFAULT_WELCOME_FILENAMES_LIST);
	}

	public String getAppDisplayName() {
		return appDisplayName;
	}

	public String getDefaultMimetype() {
		return defaultMimetype;
	}

	public String getDefaultEncoding() {
		return defaultEncoding;
	}

	public String getWelcomeExtensionsList() {
		return welcomeExtensionsList;
	}

	public String getWelcomeFilenamesList() {
		return welcomeFilenamesList;
	}

}
