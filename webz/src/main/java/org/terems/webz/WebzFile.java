package org.terems.webz;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

/** TODO !!! describe !!! **/
public interface WebzFile {

	/** TODO !!! describe !!! **/
	public String getPathName() throws IOException, WebzException;

	// TODO remove this commented out piece completely?
	// public boolean exits() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzFileMetadata getMetadata() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public byte[] getFileContent(long expectedNumberOfBytes) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public byte[] getFileContent() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzFileMetadata fileContentToOutputStream(OutputStream out) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public Collection<WebzFile> getChildren() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzFileMetadata createFolder() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzFileMetadata uploadFile(byte[] content) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzFileMetadata move(WebzFile destFile) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzFileMetadata copy(WebzFile destFile) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzFileMetadata move(String destPathName) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzFileMetadata copy(String destPathName) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public void delete() throws IOException, WebzException;

}
