package org.terems.webz;

import java.io.IOException;
import java.io.OutputStream;

public interface WebzFileSystem {

	public WebzFile get(String pathName);

	// ~

	public WebzFile _fetchMetadata(WebzFile file) throws IOException, WebzException;

	public WebzFileMetadata.FolderSpecific _fetchMetadataWithChildren(WebzFile file, WebzFileMetadata<Object> metadata)
			throws IOException, WebzException;

	public WebzFile _fileContentToOutputStream(WebzFile file, OutputStream out) throws IOException, WebzException;

	public WebzFile _createFolder(WebzFile file) throws IOException, WebzException;

	public WebzFile _uploadFile(WebzFile file, byte[] content) throws IOException, WebzException;

	public WebzFile _move(WebzFile srcFile, WebzFile destFile) throws IOException, WebzException;

	public WebzFile _copy(WebzFile srcFile, WebzFile destFile) throws IOException, WebzException;

	public void _delete(WebzFile file) throws IOException, WebzException;

}
