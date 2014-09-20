package org.terems.webz.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.terems.webz.ParentChildrenMetadata;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzMetadata;
import org.terems.webz.WebzMetadata.FileSpecific;
import org.terems.webz.base.BaseForwardSlashFileSystem;

public class LocalFileSystem extends BaseForwardSlashFileSystem {

	// TODO implement LocalFileSystem - come up with cross-platform "path name lower casing" strategy ?

	@Override
	public void init(Properties properties) {
		// Auto-generated method stub
	}

	@Override
	public String getFileSystemUniqueId() {
		// Auto-generated method stub
		return null;
	}

	@Override
	public WebzMetadata getMetadata(String pathName) throws IOException, WebzException {
		// Auto-generated method stub
		return null;
	}

	@Override
	public ParentChildrenMetadata getParentChildrenMetadata(String parentPathName) throws IOException, WebzException {
		// Auto-generated method stub
		return null;
	}

	@Override
	public WebzFileDownloader getFileDownloader(String pathName) throws IOException, WebzException {
		// Auto-generated method stub
		return null;
	}

	@Override
	public WebzMetadata createFolder(String pathName) throws IOException, WebzException {
		// Auto-generated method stub
		return null;
	}

	@Override
	public FileSpecific uploadFile(String pathName, InputStream content, long numBytes) throws IOException, WebzException {
		// Auto-generated method stub
		return null;
	}

	@Override
	public FileSpecific uploadFile(String pathName, InputStream content) throws IOException, WebzException {
		// Auto-generated method stub
		return null;
	}

	@Override
	public WebzMetadata move(String srcPathName, String destPathName) throws IOException, WebzException {
		// Auto-generated method stub
		return null;
	}

	@Override
	public WebzMetadata copy(String srcPathName, String destPathName) throws IOException, WebzException {
		// Auto-generated method stub
		return null;
	}

	@Override
	public void delete(String pathName) throws IOException, WebzException {
		// Auto-generated method stub
	}

}
