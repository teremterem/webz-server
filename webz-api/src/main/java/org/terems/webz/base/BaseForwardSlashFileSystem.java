package org.terems.webz.base;

import java.util.regex.Pattern;

/** TODO !!! describe !!! **/
public abstract class BaseForwardSlashFileSystem extends BaseWebzFileSystem {

	protected static final char FWD_SLASH = '/';
	protected static final String FWD_SLASH_STR = "" + FWD_SLASH;

	/** TODO !!! describe !!! **/
	@Override
	public String normalizePathname(String nonNormalizedPathname) {

		if (nonNormalizedPathname == null) {
			return null;
		}

		String pathname = nonNormalizedPathname.replace('\\', FWD_SLASH);

		if (pathname.startsWith(FWD_SLASH_STR)) {
			pathname = pathname.substring(1);
		}
		if (pathname.endsWith(FWD_SLASH_STR)) {
			pathname = pathname.substring(0, pathname.length() - 1);
		}

		return pathname;
	}

	private final static Pattern MULTIPLE_PATH_SEPARATORS = Pattern.compile(FWD_SLASH_STR + "{2,}");

	/** TODO !!! describe !!! **/
	@Override
	public boolean isNormalizedPathnameInvalid(String pathname) {

		return pathname == null || pathname.startsWith(FWD_SLASH_STR) || pathname.endsWith(FWD_SLASH_STR)
				|| MULTIPLE_PATH_SEPARATORS.matcher(pathname).find();
	}

	/** TODO !!! describe !!! **/
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

	/** TODO !!! describe !!! **/
	@Override
	public String concatPathname(String basePath, String relativePathname) {

		return basePath + FWD_SLASH + relativePathname;
	}

}
