package org.terems.webz.base;

import java.io.IOException;
import java.io.OutputStream;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileMetadata;
import org.terems.webz.WebzFileSystem;

public abstract class WebzFileSystemProxy extends BaseWebzFileSystem {

	protected abstract WebzFileSystem getFileSystem();

	@Override
	public WebzFile _fetchMetadata(WebzFile file) throws IOException, WebzException {
		return getFileSystem()._fetchMetadata(file);
	}

	@Override
	public WebzFileMetadata.FolderSpecific _fetchMetadataWithChildren(WebzFile file, WebzFileMetadata<Object> metadata)
			throws IOException, WebzException {
		return getFileSystem()._fetchMetadataWithChildren(file, metadata);
	}

	@Override
	public WebzFile _fileContentToOutputStream(WebzFile file, OutputStream out) throws IOException, WebzException {
		return getFileSystem()._fileContentToOutputStream(file, out);
	}

	@Override
	public WebzFile _createFolder(WebzFile file) throws IOException, WebzException {
		return getFileSystem()._createFolder(file);
	}

	@Override
	public WebzFile _uploadFile(WebzFile file, byte[] content) throws IOException, WebzException {
		return getFileSystem()._uploadFile(file, content);
	}

	@Override
	public WebzFile _move(WebzFile srcFile, WebzFile destFile) throws IOException, WebzException {
		return getFileSystem()._move(srcFile, destFile);
	}

	@Override
	public WebzFile _copy(WebzFile srcFile, WebzFile destFile) throws IOException, WebzException {
		return getFileSystem()._copy(srcFile, destFile);
	}

	@Override
	public void _delete(WebzFile file) throws IOException, WebzException {
		getFileSystem()._delete(file);
	}

}
