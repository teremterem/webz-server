package org.terems.webz;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface WebzFileMetadata<MO> {

	public void setMetadataObjectThreadSafe(MO metadataObject) throws IOException, WebzException;

	public String getPathName() throws IOException, WebzException;

	public String getName() throws IOException, WebzException;

	public boolean isFile() throws IOException, WebzException;

	public boolean isFolder() throws IOException, WebzException;

	public FileSpecific getFileSpecific() throws IOException, WebzException;

	public FolderSpecific getFolderSpecific() throws IOException, WebzException;

	public interface FileSpecific {

		public long getNumberOfBytes();

		public Date getLastModified();

		public String getRevision();
	}

	public interface FolderSpecific {

		public List<WebzFile> getChildren();

		public String getHash();

	}

}
