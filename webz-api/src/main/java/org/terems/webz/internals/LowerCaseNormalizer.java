package org.terems.webz.internals;

import org.terems.webz.internals.base.ForwardSlashNormalizer;
import org.terems.webz.util.WebzUtils;

// TODO delete LowerCaseNormalizer
@Deprecated
public class LowerCaseNormalizer extends ForwardSlashNormalizer {

	@Override
	public boolean isNormalizedPathnameInvalid(String pathname) {

		if (super.isNormalizedPathnameInvalid(pathname)) {
			return true;
		}

		return WebzUtils.containsUpperCaseLetters(pathname);
	}

}
