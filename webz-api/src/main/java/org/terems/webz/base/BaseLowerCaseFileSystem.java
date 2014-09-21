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
	public boolean belongsToSubtree(String pathname, String subtreePathname) {

		if (subtreePathname.isEmpty()) {
			return true;
		}
		if (pathname.length() < subtreePathname.length()) {
			return false;
		}
		if (pathname.length() == subtreePathname.length()) {
			return pathname.equals(subtreePathname);
		}

		if (!pathname.startsWith(subtreePathname)) {
			return false;
		}
		return pathname.codePointAt(subtreePathname.length()) == FWD_SLASH;
	}

}
