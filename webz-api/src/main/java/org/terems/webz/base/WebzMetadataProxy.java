package org.terems.webz.base;

import java.io.IOException;
import java.util.Date;

import org.terems.webz.WebzException;
import org.terems.webz.WebzMetadata;

/** TODO !!! describe !!! **/
public abstract class WebzMetadataProxy extends BaseWebzMetadata {

	/** TODO !!! describe !!! **/
	protected abstract WebzMetadata getInnerMetadata() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	@Override
	public String getName() throws IOException, WebzException {
		return getInnerMetadata().getName();
	}

	/** TODO !!! describe !!! **/
	@Override
	public boolean isFile() throws IOException, WebzException {
		return getInnerMetadata().isFile();
	}

	/** TODO !!! describe !!! **/
	@Override
	public boolean isFolder() throws IOException, WebzException {
		return getInnerMetadata().isFolder();
	}

	/** TODO !!! describe !!! **/
	@Override
	public long getNumberOfBytes() throws IOException, WebzException {
		return getInnerMetadata().getFileSpecific().getNumberOfBytes();
	}

	/** TODO !!! describe !!! **/
	@Override
	public Date getLastModified() throws IOException, WebzException {
		return getInnerMetadata().getFileSpecific().getLastModified();
	}

	/** TODO !!! describe !!! **/
	@Override
	public String getRevision() throws IOException, WebzException {
		return getInnerMetadata().getFileSpecific().getRevision();
	}

}
