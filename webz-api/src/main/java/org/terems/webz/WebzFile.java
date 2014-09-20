package org.terems.webz;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

/** TODO !!! describe !!! **/
public interface WebzFile {

	/** TODO !!! describe !!! **/
	public String getPathname();

	/** TODO !!! describe !!! **/
	public boolean isPathnameInvalid();

	/** TODO !!! describe !!! **/
	public WebzFile getParent() throws WebzPathnameException;

	/** TODO !!! describe !!! **/
	public WebzFile getDescendant(String relativePathname) throws WebzPathnameException;

	/** TODO !!! describe !!! **/
	public void inflate() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata getMetadata() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzFileDownloader getFileDownloader() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata copyContentToOutputStream(OutputStream out) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public byte[] getFileContent() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public Collection<WebzFile> listChildren() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata createFolder() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata uploadFile(InputStream content, long numBytes) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata uploadFile(InputStream content) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata uploadFile(byte[] content) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata move(WebzFile destFile) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata copy(WebzFile destFile) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata move(String destPathname) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata copy(String destPathname) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public void delete() throws IOException, WebzException;

}
