package org.terems.webz.base;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

import org.terems.webz.FreshParentChildrenMetadata;
import org.terems.webz.ParentChildrenMetadata;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzFileSystem;
import org.terems.webz.WebzMetadata;
import org.terems.webz.cache.WebzFileSystemCache;

/**
 * Basic implementation of WebzFileSystem to be extended by concrete implementations...
 **/
public abstract class BaseWebzFileSystem implements WebzFileSystem {

	/** TODO !!! describe !!! **/
	@Override
	public String normalizePathName(String pathName) {

		pathName = pathName.replace('\\', '/');

		if (pathName.startsWith("/")) {
			pathName = pathName.substring(1);
		}
		if (pathName.endsWith("/")) {
			pathName = pathName.substring(0, pathName.length() - 1);
		}
		return pathName;
	}

	private final static Pattern MULTIPLE_PATH_SEPARATORS = Pattern.compile("/{2,}");

	/** TODO !!! describe !!! **/
	protected boolean isPathNameValid(String pathName) {

		return !(pathName == null || pathName.startsWith("/") || pathName.endsWith("/") || MULTIPLE_PATH_SEPARATORS.matcher(pathName)
				.find());
	}

	/** TODO !!! describe !!! **/
	protected void assertPathNameValid(String pathName) {

		if (!isPathNameValid(pathName)) {
			throw new IllegalArgumentException(pathName + " is not a valid path name");
		}
	}

	/** TODO !!! describe !!! **/
	@Override
	public String getParentPathName(String pathName) {

		assertPathNameValid(pathName);

		if ("".equals(pathName)) {
			return null;
		}

		int separatorIndex = pathName.lastIndexOf('/');

		if (separatorIndex < 0) {
			return "";
		}
		return pathName.substring(0, separatorIndex);
	}

	/** Do nothing by default... **/
	@Override
	public void inflate(WebzFile file) throws IOException, WebzException {
	}

	/** Do nothing by default... **/
	@Override
	public void inflate(WebzFileSystemCache fileSystemCache, WebzFile file) throws IOException, WebzException {
	}

	/** Default implementation... **/
	@Override
	public FreshParentChildrenMetadata getParentChildrenMetadataIfChanged(String parentPathName, Object previousFolderHash)
			throws IOException, WebzException {

		ParentChildrenMetadata parentChildrenMetadata = getParentChildrenMetadata(parentPathName);
		if (previousFolderHash != null && parentChildrenMetadata != null && previousFolderHash.equals(parentChildrenMetadata.folderHash)) {
			return null;
		}

		return new FreshParentChildrenMetadata(parentChildrenMetadata);
	}

	/** Default implementation... **/
	@Override
	public Map<String, WebzMetadata> getChildPathNamesAndMetadata(String parentPathName) throws IOException, WebzException {
		ParentChildrenMetadata parentChildrenMetadata = getParentChildrenMetadata(parentPathName);
		return parentChildrenMetadata == null ? null : parentChildrenMetadata.childPathNamesAndMetadata;
	}

	/** Default implementation... **/
	@Override
	public Collection<String> getChildPathNames(String parentPathName) throws IOException, WebzException {

		ParentChildrenMetadata parentChildrenMetadata = getParentChildrenMetadata(parentPathName);
		if (parentChildrenMetadata == null || parentChildrenMetadata.childPathNamesAndMetadata == null) {
			return null;
		}

		return parentChildrenMetadata.childPathNamesAndMetadata.keySet();
	}

	/** Default implementation... **/
	@Override
	public WebzMetadata.FileSpecific copyContentToOutputStream(String pathName, OutputStream out) throws IOException, WebzException {

		WebzFileDownloader downloader = getFileDownloader(pathName);
		if (downloader == null) {
			return null;
		}

		downloader.copyContentAndClose(out);

		return downloader.fileSpecific;
	}

	/** Do nothing by default... **/
	@Override
	public void destroy() {
	}

}
