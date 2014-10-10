package org.terems.webz.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzMetadata;
import org.terems.webz.WebzMetadata.FileSpecific;
import org.terems.webz.internals.ParentChildrenMetadata;
import org.terems.webz.internals.base.BaseWebzFileSystemImpl;

public class LocalFileSystem extends BaseWebzFileSystemImpl {

	private static final Logger LOG = LoggerFactory.getLogger(LocalFileSystem.class);

	// TODO come up with cross-platform "pathname lower-casing ?" strategy

	@Override
	protected void init() {
		uniqueId = "localhost-" + basePath;

		if (LOG.isInfoEnabled()) {
			LOG.info("'" + uniqueId + "' file system was created");
		}
	}

	@Override
	public WebzMetadata getMetadata(String pathname) throws IOException, WebzException {

		File file = new File(basePath, pathname);
		return file.exists() ? new LocalFileMetadata(file) : null;
	}

	@Override
	public ParentChildrenMetadata getParentChildrenMetadata(String parentPathname) throws IOException, WebzException {

		File file = new File(basePath, parentPathname);
		if (!file.exists()) {
			return null;
		}

		ParentChildrenMetadata parentChildren = new ParentChildrenMetadata();
		parentChildren.parentMetadata = new LocalFileMetadata(file);

		String[] children = file.list();
		if (children != null) {

			String localBasePath = file.getAbsolutePath();
			Map<String, WebzMetadata> pathnamesAndMetadata = new HashMap<>();
			parentChildren.childPathnamesAndMetadata = pathnamesAndMetadata;
			for (String childName : children) {
				File child = new File(localBasePath, childName);
				pathnamesAndMetadata.put(pathNormalizer.concatPathname(parentPathname, childName), new LocalFileMetadata(child));
			}
		}
		return parentChildren;
	}

	@Override
	public WebzFileDownloader getFileDownloader(String pathname) throws IOException, WebzException {

		File file = new File(basePath, pathname);
		if (!file.exists() || !file.isFile()) {
			return null;
		}

		return new WebzFileDownloader(new LocalFileMetadata(file).getFileSpecific(), new FileInputStream(file));
	}

	// TODO implement LocalFileSystem

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
