package org.terems.webz.impl;

import java.io.File;
import java.io.IOException;

import org.terems.webz.WebzException;
import org.terems.webz.WebzMetadata;
import org.terems.webz.base.BaseWebzMetadata;

public class LocalFileMetadata extends BaseWebzMetadata implements WebzMetadata {

	private File file;

	public LocalFileMetadata(File file) {
		this.file = file;
	}

	@Override
	public long getNumberOfBytes() throws IOException, WebzException {
		return file.length();
	}

	@Override
	public Long getLastModified() throws IOException, WebzException {
		return file.lastModified();
	}

	@Override
	public String getRevision() throws IOException, WebzException {
		return String.valueOf(getLastModified());
	}

	@Override
	public String getName() throws IOException, WebzException {
		return file.getName();
	}

	@Override
	public boolean isFile() throws IOException, WebzException {
		return file.isFile();
	}

	@Override
	public boolean isFolder() throws IOException, WebzException {
		return file.isDirectory();
	}

}
