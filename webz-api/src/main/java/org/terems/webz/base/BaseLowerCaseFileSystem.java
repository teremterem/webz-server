package org.terems.webz.base;

import org.terems.webz.util.WebzUtils;

/** TODO !!! describe !!! **/
public abstract class BaseLowerCaseFileSystem extends BaseForwardSlashFileSystem {

	/** TODO !!! describe !!! **/
	@Override
	public boolean isNormalizedPathNameInvalid(String pathName) {

		if (super.isNormalizedPathNameInvalid(pathName)) {
			return true;
		}

		return WebzUtils.containsUpperCaseLetters(pathName);
	}

}
