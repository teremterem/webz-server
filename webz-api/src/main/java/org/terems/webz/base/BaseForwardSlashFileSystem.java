package org.terems.webz.base;

import java.util.regex.Pattern;

public abstract class BaseForwardSlashFileSystem extends BaseWebzFileSystem {

	private static final char FWD_SLASH = '/';
	private static final String FWD_SLASH_STR = "" + FWD_SLASH;

	/** TODO !!! describe !!! **/
	@Override
	public String normalizePathName(String nonNormalizedPathName) {

		if (nonNormalizedPathName == null) {
			return null;
		}

		String pathName = nonNormalizedPathName.replace('\\', FWD_SLASH);

		if (pathName.startsWith(FWD_SLASH_STR)) {
			pathName = pathName.substring(1);
		}
		if (pathName.endsWith(FWD_SLASH_STR)) {
			pathName = pathName.substring(0, pathName.length() - 1);
		}

		return pathName;
	}

	private final static Pattern MULTIPLE_PATH_SEPARATORS = Pattern.compile(FWD_SLASH_STR + "{2,}");

	/** TODO !!! describe !!! **/
	@Override
	public boolean isNormalizedPathNameInvalid(String pathName) {

		return pathName == null || pathName.startsWith(FWD_SLASH_STR) || pathName.endsWith(FWD_SLASH_STR)
				|| MULTIPLE_PATH_SEPARATORS.matcher(pathName).find();
	}

	/** TODO !!! describe !!! **/
	@Override
	public String getParentPathName(String pathName) {

		if ("".equals(pathName)) {
			return null;
		}

		int separatorIndex = pathName.lastIndexOf(FWD_SLASH);

		if (separatorIndex < 0) {
			return "";
		}
		return pathName.substring(0, separatorIndex);
	}

	/** TODO !!! describe !!! **/
	@Override
	public String concatPathName(String basePathName, String relativePathName) {

		return basePathName + FWD_SLASH + relativePathName;
	}

}
