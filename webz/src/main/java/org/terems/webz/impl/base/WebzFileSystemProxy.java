package org.terems.webz.impl.base;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileMetadata;
import org.terems.webz.impl.GenericWebzFile;
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
	public String _getFileSystemUniqueId() {
		return getFileSystem()._getFileSystemUniqueId();
	}

	@Override
	public WebzFileMetadata _getMetadata(String pathName) throws IOException, WebzException {
		return getFileSystem()._getMetadata(pathName);
	}

	@Override
	public ParentChildrenMetadata _getParentChildrenMetadata(String parentPathName) throws IOException, WebzException {
		return getFileSystem()._getParentChildrenMetadata(parentPathName);
	}

	@Override
	public FreshParentChildrenMetadata _getParentChildrenMetadataIfChanged(String parentPathName, Object previousFolderHash)
			throws IOException, WebzException {
		return getFileSystem()._getParentChildrenMetadataIfChanged(parentPathName, previousFolderHash);
	}

	@Override
	public Map<String, WebzFileMetadata> _getChildPathNamesAndMetadata(String parentPathName) throws IOException, WebzException {
		return getFileSystem()._getChildPathNamesAndMetadata(parentPathName);
	}

	@Override
	public Collection<String> _getChildPathNames(String parentPathName) throws IOException, WebzException {
		return getFileSystem()._getChildPathNames(parentPathName);
	}

	@Override
	public WebzFileMetadata _fileContentToOutputStream(String pathName, OutputStream out) throws IOException, WebzException {
		return getFileSystem()._fileContentToOutputStream(pathName, out);
	}

	@Override
	public WebzFileDownloader _getFileContentDownloader(String pathName) throws IOException, WebzException {
		return getFileSystem()._getFileContentDownloader(pathName);
	}

	@Override
	public WebzFileMetadata _createFolder(String pathName) throws IOException, WebzException {
		return getFileSystem()._createFolder(pathName);
	}

	@Override
	public WebzFileMetadata _uploadFile(String pathName, byte[] content) throws IOException, WebzException {
		return getFileSystem()._uploadFile(pathName, content);
	}

	@Override
	public WebzFileMetadata _move(String srcPathName, String destPathName) throws IOException, WebzException {
		return getFileSystem()._move(srcPathName, destPathName);
	}

	@Override
	public WebzFileMetadata _copy(String srcPathName, String destPathName) throws IOException, WebzException {
		return getFileSystem()._copy(srcPathName, destPathName);
	}

	@Override
	public void _delete(String pathName) throws IOException, WebzException {
		getFileSystem()._delete(pathName);
	}

}
