package org.terems.webz.impl;

import java.util.regex.Pattern;

import org.terems.webz.base.BaseWebzPropertiesInitable;
import org.terems.webz.internals.WebzPathNormalizer;

public class ForwardSlashNormalizer extends BaseWebzPropertiesInitable implements WebzPathNormalizer {

	protected static final char FWD_SLASH = '/';
	protected static final String FWD_SLASH_STR = "" + FWD_SLASH;

	@Override
	public String normalizePathname(String nonNormalizedPathname, boolean trimLeadingSlash) {

		if (nonNormalizedPathname == null) {
			return null;
		}

		String pathname = nonNormalizedPathname.replace('\\', FWD_SLASH);

		if (trimLeadingSlash && pathname.startsWith(FWD_SLASH_STR)) {
			pathname = pathname.substring(1);
		}
		if (pathname.endsWith(FWD_SLASH_STR)) {
			pathname = pathname.substring(0, pathname.length() - 1);
		}

		return pathname;
	}

	// protecting from multiple path separators ("//", "///" and so on)
	private final static Pattern AMBIGUOUS_CHAR_SEQUENCES = Pattern.compile(FWD_SLASH + "{2,}");
	// protecting from "." and ".." path members
	private final static Pattern RESERVED_PATH_MEMBERS = Pattern.compile("(^|" + FWD_SLASH + ")\\.{1,2}($|" + FWD_SLASH + ")");

	@Override
	public boolean isNormalizedPathnameInvalid(String pathname) {

		return pathname == null || pathname.startsWith(FWD_SLASH_STR) || pathname.endsWith(FWD_SLASH_STR)
				|| AMBIGUOUS_CHAR_SEQUENCES.matcher(pathname).find() || RESERVED_PATH_MEMBERS.matcher(pathname).find();
	}

	// pathname is considered hidden if one of its path members start with the dot (for ex. ".webz/general.properties")
	private final static Pattern HIDDEN_PATH_MEMBERS = Pattern.compile("(^|" + FWD_SLASH + ")\\.");

	@Override
	public boolean isHidden(String pathname) {
		return HIDDEN_PATH_MEMBERS.matcher(pathname).find();
	}

	@Override
	public boolean belongsToSubtree(String pathname, String subtreePath) {

		if (subtreePath.isEmpty()) {
			return true;
		}
		if (pathname.length() < subtreePath.length()) {
			return false;
		}

		if (!pathname.startsWith(subtreePath)) {
			return false;
		}
		return pathname.length() == subtreePath.length() || pathname.codePointAt(subtreePath.length()) == FWD_SLASH;
	}

	@Override
	public String getParentPathname(String pathname) {

		if ("".equals(pathname)) {
			return null;
		}

		int separatorIndex = pathname.lastIndexOf(FWD_SLASH);

		if (separatorIndex < 0) {
			return "";
		}
		return pathname.substring(0, separatorIndex);
	}

	@Override
	public String concatPathname(String basePath, String relativePathname) {

		return basePath + FWD_SLASH + relativePathname;
	}

}
