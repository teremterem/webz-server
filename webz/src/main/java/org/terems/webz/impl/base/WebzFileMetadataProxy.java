package org.terems.webz.impl.base;

import java.io.IOException;
import java.util.Date;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFileMetadata;

public abstract class WebzFileMetadataProxy extends BaseWebzFileMetadata {

	protected abstract WebzFileMetadata getInnerMetadata() throws IOException, WebzException;

	@Override
	public String getName() throws IOException, WebzException {
		WebzFileMetadata metadata = getInnerMetadata();
		return metadata == null ? null : metadata.getName();
	}

	@Override
	public boolean isFile() throws IOException, WebzException {
		WebzFileMetadata metadata = getInnerMetadata();
		return metadata == null ? false : metadata.isFile();
	}

	@Override
	public boolean isFolder() throws IOException, WebzException {
		WebzFileMetadata metadata = getInnerMetadata();
		return metadata == null ? false : metadata.isFolder();
	}

	@Override
	public FileSpecific getFileSpecific() throws IOException, WebzException {
		WebzFileMetadata metadata = getInnerMetadata();
		return metadata == null ? null : metadata.getFileSpecific();
	}

	@Override
	public String _getNativePathName() throws IOException, WebzException {
		WebzFileMetadata metadata = getInnerMetadata();
		return metadata == null ? null : metadata._getNativePathName();
	}

	@Override
	public long getNumberOfBytes() throws IOException, WebzException {
		WebzFileMetadata.FileSpecific fileSpecific = getInnerMetadata().getFileSpecific();
		return fileSpecific == null ? 0 : fileSpecific.getNumberOfBytes();
	}

	@Override
	public Date getLastModified() throws IOException, WebzException {
		WebzFileMetadata.FileSpecific fileSpecific = getInnerMetadata().getFileSpecific();
		return fileSpecific == null ? null : fileSpecific.getLastModified();
	}

	@Override
	public String getRevision() throws IOException, WebzException {
		WebzFileMetadata.FileSpecific fileSpecific = getInnerMetadata().getFileSpecific();
		return fileSpecific == null ? null : fileSpecific.getRevision();
	}

}
