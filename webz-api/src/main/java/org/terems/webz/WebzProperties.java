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

package org.terems.webz;

import java.util.HashMap;
import java.util.Map;

import org.terems.webz.util.WebzUtils;

/**
 * {@code WebzProperties} is an alternative to traditional {@code java.util.Properties} class. Its main benefit is that it doesn't extend
 * {@code java.util.Hashtable} and will not become a bottleneck in case of concurrent reading.
 **/
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

	/** TODO !!! describe !!! **/
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public WebzProperties(WebzProperties properties) {
		this((Map) properties);
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
