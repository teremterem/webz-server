package org.terems.webz;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

/** TODO !!! describe !!! **/
public abstract class BaseWebzFileSystem implements WebzFileSystem {

	/**
	 * Default implementation always returns an instance of {@link FreshParentChildrenMetadata} (never returns null) implying that the data
	 * is always "fresh" (however {@code FreshParentChildrenMetadata.parentChildrenMetadata} field may still be null)...
	 * 
	 * @param parentPathName
	 * @param previousFolderHash
	 *            default implementation always ignores it.
	 * @return always returns an instance of {@link FreshParentChildrenMetadata} (never returns null).
	 **/
	@Override
	public FreshParentChildrenMetadata getParentChildrenMetadataIfChanged(String parentPathName, Object previousFolderHash)
			throws IOException, WebzException {
		return new FreshParentChildrenMetadata(getParentChildrenMetadata(parentPathName));
	}

	/**
	 * Default implementation...
	 **/
	@Override
	public Map<String, WebzMetadata> getChildPathNamesAndMetadata(String parentPathName) throws IOException, WebzException {
		ParentChildrenMetadata parentChildrenMetadata = getParentChildrenMetadata(parentPathName);
		return parentChildrenMetadata == null ? null : parentChildrenMetadata.childPathNamesAndMetadata;
	}

	/**
	 * Default implementation...
	 **/
	@Override
	public Collection<String> getChildPathNames(String parentPathName) throws IOException, WebzException {

		ParentChildrenMetadata parentChildrenMetadata = getParentChildrenMetadata(parentPathName);
		if (parentChildrenMetadata == null || parentChildrenMetadata.childPathNamesAndMetadata == null) {
			return null;
		}

		return parentChildrenMetadata.childPathNamesAndMetadata.keySet();
	}

	/**
	 * Default implementation...
	 **/
	@Override
	public WebzMetadata.FileSpecific fileContentToOutputStream(String pathName, OutputStream out) throws IOException, WebzException {
		return fileContentToOutputStream(this, pathName, out);
	}

	/** TODO !!! describe !!! **/
	public static WebzMetadata.FileSpecific fileContentToOutputStream(WebzFileSystem fileSystem, String pathName, OutputStream out)
			throws IOException, WebzException {

		WebzFileDownloader downloader = fileSystem.getFileContentDownloader(pathName);
		if (downloader == null) {
			return null;
		}

		downloader.fileContentToOutputStream(out);

		return downloader.fileSpecific;
	}

}
