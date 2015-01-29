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
		Properties mimetypesProperties = WebzUtils.loadProperties(file);

		// TODO do not fail if properties file is absent (warn ?)

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
