package org.terems.webz.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileMetadata;
import org.terems.webz.internal.FreshParentChildrenMetadata;
import org.terems.webz.internal.ParentChildrenMetadata;
import org.terems.webz.internal.WebzFileDownloader;
import org.terems.webz.internal.WebzFileSystem;

public abstract class BaseWebzFileSystem implements WebzFileSystem {

	@Override
	public WebzFile get(String pathName) {
		return new GenericWebzFile(this, pathName);
	}

	/**
	 * Default implementation always returns an instance of {@link FreshParentChildrenMetadata} (never returns null) implying
	 * that the data is always "fresh" (however {@code FreshParentChildrenMetadata.parentChildrenMetadata} field may still be
	 * null)...
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
	public Map<String, WebzFileMetadata> getChildPathNamesAndMetadata(String parentPathName) throws IOException, WebzException {
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
	public WebzFileMetadata fileContentToOutputStream(String pathName, OutputStream out) throws IOException, WebzException {
		return fileContentToOutputStream(this, pathName, out);
	}

	public static WebzFileMetadata fileContentToOutputStream(WebzFileSystem fileSystem, String pathName, OutputStream out)
			throws IOException, WebzException {
		WebzFileDownloader downloader = fileSystem.getFileContentDownloader(pathName);
		downloader.fileContentToOutputStream(out);
		return downloader.metadata;
	}

}
