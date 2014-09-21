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

	// TODO implement LocalFileSystem - come up with cross-platform "pathname lower-casing" strategy ?

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
	public boolean belongsToSubtree(String pathname, String subtreePathname) {
		// Auto-generated method stub
		return false;
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
