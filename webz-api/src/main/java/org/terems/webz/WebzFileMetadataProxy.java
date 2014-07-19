package org.terems.webz;

import java.io.IOException;
import java.util.Date;

/** TODO !!! describe !!! **/
public abstract class WebzFileMetadataProxy extends BaseWebzFileMetadata {

	/** TODO !!! describe !!! **/
	protected abstract WebzFileMetadata getInnerMetadata() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	@Override
	public String getName() throws IOException, WebzException {
		WebzFileMetadata metadata = getInnerMetadata();
		return metadata == null ? null : metadata.getName();
	}

	/** TODO !!! describe !!! **/
	@Override
	public boolean isFile() throws IOException, WebzException {
		WebzFileMetadata metadata = getInnerMetadata();
		return metadata == null ? false : metadata.isFile();
	}

	/** TODO !!! describe !!! **/
	@Override
	public boolean isFolder() throws IOException, WebzException {
		WebzFileMetadata metadata = getInnerMetadata();
		return metadata == null ? false : metadata.isFolder();
	}

	/** TODO !!! describe !!! **/
	@Override
	public FileSpecific getFileSpecific() throws IOException, WebzException {
		WebzFileMetadata metadata = getInnerMetadata();
		return metadata == null ? null : metadata.getFileSpecific();
	}

	/** TODO !!! describe !!! **/
	@Override
	public String _getNativePathName() throws IOException, WebzException {
		WebzFileMetadata metadata = getInnerMetadata();
		return metadata == null ? null : metadata._getNativePathName();
	}

	/** TODO !!! describe !!! **/
	@Override
	public long getNumberOfBytes() throws IOException, WebzException {
		WebzFileMetadata.FileSpecific fileSpecific = getInnerMetadata().getFileSpecific();
		return fileSpecific == null ? 0 : fileSpecific.getNumberOfBytes();
	}

	/** TODO !!! describe !!! **/
	@Override
	public Date getLastModified() throws IOException, WebzException {
		WebzFileMetadata.FileSpecific fileSpecific = getInnerMetadata().getFileSpecific();
		return fileSpecific == null ? null : fileSpecific.getLastModified();
	}

	/** TODO !!! describe !!! **/
	@Override
	public String getRevision() throws IOException, WebzException {
		WebzFileMetadata.FileSpecific fileSpecific = getInnerMetadata().getFileSpecific();
		return fileSpecific == null ? null : fileSpecific.getRevision();
	}

}
