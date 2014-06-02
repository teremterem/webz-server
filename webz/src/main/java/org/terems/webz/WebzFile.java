package org.terems.webz;

import java.io.IOException;
import java.io.OutputStream;

public interface WebzFile extends WebzFileMetadata<Object> {

	public boolean exits() throws IOException, WebzException;

	public void setMetadataThreadSafe(WebzFileMetadata<Object> metadata);

	public byte[] getFileContent(long expectedNumberOfBytes) throws IOException, WebzException;

	public byte[] getFileContent() throws IOException, WebzException;

	public WebzFile fileContentToOutputStream(OutputStream out) throws IOException, WebzException;

	public WebzFile createFolder() throws IOException, WebzException;

	public WebzFile uploadFile(byte[] content) throws IOException, WebzException;

	public WebzFile move(WebzFile destFile) throws IOException, WebzException;

	public WebzFile copy(WebzFile destFile) throws IOException, WebzException;

	public WebzFile move(String destPathName) throws IOException, WebzException;

	public WebzFile copy(String destPathName) throws IOException, WebzException;

}
