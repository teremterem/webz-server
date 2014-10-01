package org.terems.webz;

import java.util.HashMap;
import java.util.Map;

import org.terems.webz.util.WebzUtils;

/** TODO !!! describe !!! **/
@SuppressWarnings("serial")
public class WebzProperties extends HashMap<String, String> {

	public static final String WEBZ_CONFIG_FOLDER = "-webz-config";

	public static final String FS_IMPL_CLASS_PROPERTY = "file.system.impl.class";
	public static final String FS_CACHE_ENABLED_PROPERTY = "file.system.cache.enabled";
	public static final String FS_CACHE_IMPL_CLASS_PROPERTY = "file.system.cache.impl.class";
	public static final String FS_CACHE_PAYLOAD_THRESHOLD_BYTES_PROPERTY = "file.system.cache.payload.threshold.bytes";

	public static final String GENERAL_PROPERTIES_FILE = "general.properties";
	public static final String STATUS_CODES_PROPERTIES_FILE = "status-codes.properties";
	public static final String MIMETYPES_PROPERTIES_FILE = "mimetypes.properties";

	public static final String APP_DISPLAY_NAME_PROPERTY = "app.display.name";
	public static final String DEFAULT_MIMETYPE_PROPERTY = "default.mimetype";

	public static final String PATH_TO_404_FILE_PROPERTY = "404";
	public static final String PATH_TO_500_FILE_PROPERTY = "500";

	/** TODO !!! describe !!! **/
	public WebzProperties() {
		super();
	}

	/** TODO !!! describe !!! **/
	public WebzProperties(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	/** TODO !!! describe !!! **/
	public WebzProperties(int initialCapacity) {
		super(initialCapacity);
	}

	/** TODO !!! describe !!! **/
	public WebzProperties(Map<Object, Object> properties) {

		super(properties.size(), 1);

		for (Map.Entry<Object, Object> entry : properties.entrySet()) {
			put(WebzUtils.assertString(entry.getKey()), WebzUtils.assertString(entry.getValue()));
		}
	}

}
