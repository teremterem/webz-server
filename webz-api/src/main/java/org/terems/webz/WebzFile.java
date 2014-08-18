package org.terems.webz;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

/** TODO !!! describe !!! **/
public interface WebzFile {

	/** TODO !!! describe !!! **/
	public String getActualPathName() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzFileMetadata getMetadata() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public byte[] getFileContent() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzFileMetadata fileContentToOutputStream(OutputStream out) throws IOException, WebzException;

	// TODO consider exposing input stream as well ?

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
