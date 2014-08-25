package org.terems.webz;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

/** TODO !!! describe !!! **/
public interface WebzFile {

	/** TODO !!! describe !!! **/
	public String getPathName() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public void inflate() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata getMetadata() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public byte[] getFileContent() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata fileContentToOutputStream(OutputStream out) throws IOException, WebzException;

	// TODO consider exposing input stream as well ?

	/** TODO !!! describe !!! **/
	public WebzFile getParent() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public Collection<WebzFile> getChildren() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata createFolder() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata uploadFile(byte[] content) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata move(WebzFile destFile) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata copy(WebzFile destFile) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata move(String destPathName) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata copy(String destPathName) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public void delete() throws IOException, WebzException;

}
