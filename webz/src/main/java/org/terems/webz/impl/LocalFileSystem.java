package org.terems.webz.impl;

import java.io.IOException;
import java.io.InputStream;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzMetadata;
import org.terems.webz.WebzMetadata.FileSpecific;
import org.terems.webz.internals.ParentChildrenMetadata;
import org.terems.webz.internals.base.BaseWebzFileSystemImpl;

public class LocalFileSystem extends BaseWebzFileSystemImpl {

	// TODO come up with cross-platform "pathname lower-casing ?" strategy

	// TODO implement LocalFileSystem

	@Override
	protected void init() {
		uniqueId = "localhost-" + basePath;
	}

	@Override
	public WebzMetadata getMetadata(String pathname) throws IOException, WebzException {
		// Auto-generated method stub
		return null;
	}

	@Override
	public ParentChildrenMetadata getParentChildrenMetadata(String parentPathname) throws IOException, WebzException {
		// Auto-generated method stub
		return null;
	}

	@Override
	public WebzFileDownloader getFileDownloader(String pathname) throws IOException, WebzException {
		// Auto-generated method stub
		return null;
	}

	@Override
	public WebzMetadata createFolder(String pathname) throws IOException, WebzException {
		// Auto-generated method stub
		return null;
	}

	@Override
	public FileSpecific uploadFile(String pathname, InputStream content, long numBytes) throws IOException, WebzException {
		// Auto-generated method stub
		return null;
	}

	@Override
	public FileSpecific uploadFile(String pathname, InputStream content) throws IOException, WebzException {
		// Auto-generated method stub
		return null;
	}

	@Override
	public WebzMetadata move(String srcPathname, String destPathname) throws IOException, WebzException {
		// Auto-generated method stub
		return null;
	}

	@Override
	public WebzMetadata copy(String srcPathname, String destPathname) throws IOException, WebzException {
		// Auto-generated method stub
		return null;
	}

	@Override
	public void delete(String pathname) throws IOException, WebzException {
		// Auto-generated method stub
	}

}
