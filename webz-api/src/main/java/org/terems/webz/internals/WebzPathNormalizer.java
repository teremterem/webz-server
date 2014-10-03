package org.terems.webz.internals;


/** TODO !!! describe !!! **/
public interface WebzPathNormalizer {

	/** TODO !!! describe !!! **/
	public String normalizePathname(String nonNormalizedPathname);

	/** TODO !!! describe !!! **/
	public boolean isNormalizedPathnameInvalid(String pathname);

	/** TODO !!! describe !!! **/
	public String getParentPathname(String pathname);

	/** TODO !!! describe !!! **/
	public String concatPathname(String basePath, String relativePathname);

	/** TODO !!! describe !!! **/
	public boolean belongsToSubtree(String pathname, String subtreePath);

}
