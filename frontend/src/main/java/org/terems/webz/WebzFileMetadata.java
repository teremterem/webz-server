package org.terems.webz;

import java.util.Date;

public interface WebzFileMetadata {

	public String getName();

	public String getPath();

	public boolean isFile();

	public Long getNumberOfBytes();

	public Date getLastModified();

	public String getRevision();

}
