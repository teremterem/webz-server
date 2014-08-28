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
		WebzMetadata metadata = getInnerMetadata();
		return metadata == null ? null : metadata.getName();
	}

	/** TODO !!! describe !!! **/
	@Override
	public boolean isFile() throws IOException, WebzException {
		WebzMetadata metadata = getInnerMetadata();
		return metadata == null ? false : metadata.isFile();
	}

	/** TODO !!! describe !!! **/
	@Override
	public boolean isFolder() throws IOException, WebzException {
		WebzMetadata metadata = getInnerMetadata();
		return metadata == null ? false : metadata.isFolder();
	}

	/** TODO !!! describe !!! **/
	@Override
	public long getNumberOfBytes() throws IOException, WebzException {
		WebzMetadata.FileSpecific fileSpecific = getInnerMetadata().getFileSpecific();
		return fileSpecific == null ? 0 : fileSpecific.getNumberOfBytes();
	}

	/** TODO !!! describe !!! **/
	@Override
	public Date getLastModified() throws IOException, WebzException {
		WebzMetadata.FileSpecific fileSpecific = getInnerMetadata().getFileSpecific();
		return fileSpecific == null ? null : fileSpecific.getLastModified();
	}

	/** TODO !!! describe !!! **/
	@Override
	public String getRevision() throws IOException, WebzException {
		WebzMetadata.FileSpecific fileSpecific = getInnerMetadata().getFileSpecific();
		return fileSpecific == null ? null : fileSpecific.getRevision();
	}

}
