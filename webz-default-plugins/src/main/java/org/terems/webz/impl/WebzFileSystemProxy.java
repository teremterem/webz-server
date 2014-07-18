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

public abstract class WebzFileSystemProxy implements WebzFileSystem {

	protected abstract WebzFileSystem getFileSystem();

	@Override
	public WebzFile get(String pathName) {
		return new GenericWebzFile(this, pathName);
	}

	@Override
	public String getFileSystemUniqueId() {
		return getFileSystem().getFileSystemUniqueId();
	}

	@Override
	public WebzFileMetadata getMetadata(String pathName) throws IOException, WebzException {
		return getFileSystem().getMetadata(pathName);
	}

	@Override
	public ParentChildrenMetadata getParentChildrenMetadata(String parentPathName) throws IOException, WebzException {
		return getFileSystem().getParentChildrenMetadata(parentPathName);
	}

	@Override
	public FreshParentChildrenMetadata getParentChildrenMetadataIfChanged(String parentPathName, Object previousFolderHash)
			throws IOException, WebzException {
		return getFileSystem().getParentChildrenMetadataIfChanged(parentPathName, previousFolderHash);
	}

	@Override
	public Map<String, WebzFileMetadata> getChildPathNamesAndMetadata(String parentPathName) throws IOException, WebzException {
		return getFileSystem().getChildPathNamesAndMetadata(parentPathName);
	}

	@Override
	public Collection<String> getChildPathNames(String parentPathName) throws IOException, WebzException {
		return getFileSystem().getChildPathNames(parentPathName);
	}

	@Override
	public WebzFileMetadata fileContentToOutputStream(String pathName, OutputStream out) throws IOException, WebzException {
		return getFileSystem().fileContentToOutputStream(pathName, out);
	}

	@Override
	public WebzFileDownloader getFileContentDownloader(String pathName) throws IOException, WebzException {
		return getFileSystem().getFileContentDownloader(pathName);
	}

	@Override
	public WebzFileMetadata createFolder(String pathName) throws IOException, WebzException {
		return getFileSystem().createFolder(pathName);
	}

	@Override
	public WebzFileMetadata uploadFile(String pathName, byte[] content) throws IOException, WebzException {
		return getFileSystem().uploadFile(pathName, content);
	}

	@Override
	public WebzFileMetadata move(String srcPathName, String destPathName) throws IOException, WebzException {
		return getFileSystem().move(srcPathName, destPathName);
	}

	@Override
	public WebzFileMetadata copy(String srcPathName, String destPathName) throws IOException, WebzException {
		return getFileSystem().copy(srcPathName, destPathName);
	}

	@Override
	public void delete(String pathName) throws IOException, WebzException {
		getFileSystem().delete(pathName);
	}

}
