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

	/** TODO !!! describe !!! **/
	@Override
	public boolean belongsToSubtree(String pathname, String subtreePath) {

		if (subtreePath.isEmpty()) {
			return true;
		}
		if (pathname.length() < subtreePath.length()) {
			return false;
		}
		if (pathname.length() == subtreePath.length()) {
			return pathname.equals(subtreePath);
		}

		if (!pathname.startsWith(subtreePath)) {
			return false;
		}
		return pathname.codePointAt(subtreePath.length()) == FWD_SLASH;
	}

}
