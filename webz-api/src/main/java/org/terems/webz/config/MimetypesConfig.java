package org.terems.webz.config;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzMetadata;
import org.terems.webz.util.WebzUtils;

public class MimetypesConfig extends WebzConfigObject {

	private Map<String, String> mimetypes = new HashMap<>();

	@Override
	public void init(WebzFile configFolder) throws WebzException {

		WebzFile file = configFolder.getDescendant(WebzProperties.MIMETYPES_PROPERTIES_FILE);
		Properties mimetypesProperties = WebzUtils.loadProperties(file);

		for (Map.Entry<Object, Object> entry : mimetypesProperties.entrySet()) {
			mimetypes.put(WebzUtils.assertString(entry.getKey()).toLowerCase(Locale.ENGLISH), WebzUtils.assertString(entry.getValue()));
		}
	}

	public String getMimetype(WebzMetadata metadata, String defaultMimetype) throws WebzException {

		String fileExtension = WebzUtils.getFileExtension(metadata);

		if (fileExtension == null) {
			return defaultMimetype;
		}
		String mimetype = mimetypes.get(fileExtension.toLowerCase(Locale.ENGLISH));

		return mimetype == null ? defaultMimetype : mimetype;
	}

}
