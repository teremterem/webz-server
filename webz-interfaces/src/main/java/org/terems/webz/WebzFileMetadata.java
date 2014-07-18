package org.terems.webz;

import java.io.IOException;
import java.util.Date;

/**
 * TODO !!! describe !!!
 * 
 * P.S. implementations should be immutable ?
 * 
 * P.P.S. should all implementations be Serializable ?
 **/
public interface WebzFileMetadata {

	/** TODO !!! describe !!! **/
	public String getName() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public boolean isFile() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public boolean isFolder() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public FileSpecific getFileSpecific() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public interface FileSpecific extends WebzFileMetadata {

		/** TODO !!! describe !!! **/
		public long getNumberOfBytes() throws IOException, WebzException;

		/** TODO !!! describe !!! **/
		public Date getLastModified() throws IOException, WebzException;

		/** TODO !!! describe !!! **/
		public String getRevision() throws IOException, WebzException;
	}

	// TODO either get rid of this method if it brings no value for the abstraction layer or describe !!!
	public String _getNativePathName() throws IOException, WebzException;

}
