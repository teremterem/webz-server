package org.terems.webz.base;

import org.terems.webz.util.WebzUtils;

/** TODO !!! describe !!! **/
public abstract class BaseLowerCaseFileSystem extends BaseForwardSlashFileSystem {

	/** TODO !!! describe !!! **/
	@Override
	public boolean isNormalizedPathnameInvalid(String pathname) {

		if (super.isNormalizedPathnameInvalid(pathname)) {
			return true;
		}

		return WebzUtils.containsUpperCaseLetters(pathname);
	}

}
