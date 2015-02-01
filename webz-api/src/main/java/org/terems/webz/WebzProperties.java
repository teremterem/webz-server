package org.terems.webz;

import java.util.HashMap;
import java.util.Map;

import org.terems.webz.util.WebzUtils;

/** TODO !!! describe !!! **/
@SuppressWarnings("serial")
public class WebzProperties extends HashMap<String, String> {

	/** TODO !!! describe !!! **/
	public String get(String key, String defaultValue) {

		String value = get(key);
		return value == null ? defaultValue : value;
	}

	/** TODO !!! describe !!! **/
	public WebzProperties(Map<Object, Object> properties) {

		super(properties.size(), 1);

		for (Map.Entry<Object, Object> entry : properties.entrySet()) {
			put(WebzUtils.assertString(entry.getKey()), WebzUtils.assertString(entry.getValue()));
		}
	}

	public WebzProperties() {
		super();
	}

	public WebzProperties(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	public WebzProperties(int initialCapacity) {
		super(initialCapacity);
	}

	public static final String WEBZ_CONFIG_FOLDER = ".webz";

	public static final String WEBZ_FS_IMPL_CLASS_PROPERTY = "webz.file.system.impl.class";
	public static final String FS_CACHE_ENABLED_PROPERTY = "file.system.cache.enabled";
	public static final String FS_CACHE_IMPL_CLASS_PROPERTY = "file.system.cache.impl.class";
	public static final String FS_CACHE_PAYLOAD_THRESHOLD_BYTES_PROPERTY = "file.system.cache.payload.threshold.bytes";
	public static final String FS_BASE_PATH_PROPERTY = "file.system.base.path";
	public static final String USE_METADATA_INFLATABLE_FILES_PROPERTY = "use.metadata.inflatable.files";

	public static final String GIT_ORIGIN_URL_PROPERTY = "git.origin.url";
	public static final String GIT_BRANCH_PROPERTY = "git.branch";
	public static final String GIT_REPO_LOCAL_FOLDER_NAME_PROPERTY = "git.repo.local.folder.name";
	public static final String LOCAL_STORAGE_BASE_PATH_PROPERTY = "local.storage.base.path";
	public static final String LOCAL_STORAGE_BASE_PATH_ENV_VAR_PROPERTY = "local.storage.base.path.env.var";

	public static final String GENERAL_PROPERTIES_FILE = "general.properties";
	public static final String STATUS_CODES_PROPERTIES_FILE = "status-codes.properties";
	public static final String MIMETYPES_PROPERTIES_FILE = "mimetypes.properties";

	public static final String APP_DISPLAY_NAME_PROPERTY = "app.display.name";
	public static final String DEFAULT_MIMETYPE_PROPERTY = "default.mimetype";
	public static final String DEFAULT_ENCODING_PROPERTY = "default.encoding";

	public static final String WELCOME_EXTENSIONS_PROPERTY = "welcome.extensions";
	public static final String WELCOME_FILENAMES_PROPERTY = "welcome.filenames";

	public static final String PATH_TO_404_FILE_PROPERTY = "404";
	public static final String PATH_TO_500_FILE_PROPERTY = "500";

}
