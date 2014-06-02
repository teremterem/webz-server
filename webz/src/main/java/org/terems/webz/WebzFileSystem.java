package org.terems.webz;

import java.io.IOException;
import java.io.OutputStream;

public interface WebzFileSystem {

	public WebzFile get(String pathName);

	// ~

	public WebzFileMetadata<Object> fetchMetadata(WebzFile file) throws IOException, WebzException;

	public WebzFileMetadata.FolderSpecific fetchMetadataWithChildren(WebzFile file, WebzFileMetadata<Object> metadata)
			throws IOException, WebzException;

	public WebzFile fileContentToOutputStream(WebzFile file, OutputStream out) throws IOException, WebzException;

	public WebzFile createFolder(WebzFile file) throws IOException, WebzException;

	public WebzFile uploadFile(WebzFile file, byte[] content, boolean override) throws IOException, WebzException;

	public WebzFile move(WebzFile srcFile, WebzFile destFile) throws IOException, WebzException;

	public WebzFile copy(WebzFile srcFile, WebzFile destFile) throws IOException, WebzException;

}
