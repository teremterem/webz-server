package org.terems;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

public class WebzLaunchHelper {

	public static final String SERVER_TITLE = "WebZ Server v0.9 beta";
	public static final String VERSION_TITLE = "Pedesis";

	public static final String WEBZ_PROPERTIES_PATH_PROPERTY = "webz.properties.path";
	public static final String WEBZ_PROPERTIES_PATH_ENV_VAR = WEBZ_PROPERTIES_PATH_PROPERTY.replace('.', '_').toUpperCase(Locale.ENGLISH);
	public static final String WEBZ_PROPERTIES_DEFAULT_FILENAME = "webz.properties";

	public static final String SITE_CONTENT_PATH_PROPERTY = "site.content.path";
	public static final String RENDERING_SPA_PATH_PROPERTY = "rendering.spa.path";

	public static Properties initWebzPropertiesInLauncher(File thisJarFile) throws IOException {

		Properties webzProperties = new Properties();

		String path = System.getProperty(WEBZ_PROPERTIES_PATH_PROPERTY);
		if (path == null) {
			path = System.getenv(WEBZ_PROPERTIES_PATH_ENV_VAR);

			if (path != null) {
				System.out.println();
				System.out.println(getUsingEnvVarMessage(WEBZ_PROPERTIES_PATH_ENV_VAR));
			}
		}
		File file = path == null ? new File(thisJarFile.getParent(), WEBZ_PROPERTIES_DEFAULT_FILENAME) : new File(path);

		System.setProperty(WEBZ_PROPERTIES_PATH_PROPERTY, file.getAbsolutePath());
		// setting the path to webz.properties as a java system property for it to be read by webz.war

		if (!(file.exists() && file.isFile())) {
			System.out.flush();
			System.err.println();
			System.err.println("WARNING! " + getPropertiesNotLoadedMessage(file));
			System.err.flush();
		} else {
			System.out.println();
			System.out.println(getPropertiesPathMessage(file));
			webzProperties.load(new FileInputStream(file));
		}
		System.out.println();

		return webzProperties;
	}

	public static String getUsingEnvVarMessage(String envVarName) {
		return "Using the value of " + WEBZ_PROPERTIES_PATH_ENV_VAR + " environment variable as WebZ properties path...";
	}

	public static String getPropertiesNotLoadedMessage(File webzPropertiesFile) {
		return "WebZ properties were NOT loaded: " + webzPropertiesFile.getAbsolutePath() + " does not exist or is not a file";
	}

	public static String getPropertiesPathMessage(File webzPropertiesFile) {
		return "WebZ properties path: " + webzPropertiesFile.getAbsolutePath();
	}

}
