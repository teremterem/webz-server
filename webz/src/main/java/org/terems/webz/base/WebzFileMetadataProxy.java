package org.terems.webz.base;

import java.io.IOException;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFileMetadata;

public abstract class WebzFileMetadataProxy<MO> implements WebzFileMetadata<MO> {

	protected abstract WebzFileMetadata<MO> getMetadata() throws IOException, WebzException;

	@Override
	public String getPathName() throws IOException, WebzException {
		WebzFileMetadata<MO> metadata = getMetadata();
		return metadata == null ? null : metadata.getPathName();
	}

	@Override
	public String getName() throws IOException, WebzException {
		WebzFileMetadata<MO> metadata = getMetadata();
		return metadata == null ? null : metadata.getName();
	}

	@Override
	public boolean isFile() throws IOException, WebzException {
		WebzFileMetadata<MO> metadata = getMetadata();
		return metadata == null ? false : metadata.isFile();
	}

	@Override
	public boolean isFolder() throws IOException, WebzException {
		WebzFileMetadata<MO> metadata = getMetadata();
		return metadata == null ? false : metadata.isFolder();
	}

	@Override
	public FileSpecific getFileSpecific() throws IOException, WebzException {
		WebzFileMetadata<MO> metadata = getMetadata();
		return metadata == null ? null : metadata.getFileSpecific();
	}

	@Override
	public FolderSpecific getFolderSpecific() throws IOException, WebzException {
		WebzFileMetadata<MO> metadata = getMetadata();
		return metadata == null ? null : metadata.getFolderSpecific();
	}

	@Override
	public void setMetadataObjectThreadSafe(MO metadataObject) throws IOException, WebzException {
		throw new UnsupportedOperationException();
	}

}
