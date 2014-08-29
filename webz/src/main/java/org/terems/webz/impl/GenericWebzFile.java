package org.terems.webz.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzFileFactory;
import org.terems.webz.WebzFileSystem;
import org.terems.webz.WebzMetadata;

public class GenericWebzFile implements WebzFile {

	private String pathName;
	private WebzFileFactory fileFactory;
	private WebzFileSystem fileSystem;

	private boolean inflated = false;

	public GenericWebzFile(String pathName, WebzFileFactory fileFactory, WebzFileSystem fileSystem) {
		this.pathName = pathName;
		this.fileFactory = fileFactory;
		this.fileSystem = fileSystem;
	}

	@Override
	public String getPathName() {
		return pathName;
	}

	@Override
	public void inflate() throws IOException, WebzException {
		if (!inflated) {
			fileSystem.inflate(this);
			inflated = true;
		}
	}

	@Override
	public WebzMetadata getMetadata() throws IOException, WebzException {
		return fileSystem.getMetadata(pathName);
	}

	@Override
	public WebzFileDownloader getFileDownloader() throws IOException, WebzException {
		return fileSystem.getFileDownloader(pathName);
	}

	@Override
	public WebzMetadata copyContentToOutputStream(OutputStream out) throws IOException, WebzException {
		return fileSystem.copyContentToOutputStream(pathName, out);
	}

	/**
	 * Will also fetch metadata...
	 **/
	@Override
	public byte[] getFileContent() throws IOException, WebzException {

		WebzMetadata metadata = getMetadata();
		if (metadata == null) {
			return null;
		}

		WebzMetadata.FileSpecific fileSpecific = metadata.getFileSpecific();
		if (fileSpecific == null) {
			return null;
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream((int) fileSpecific.getNumberOfBytes());
		copyContentToOutputStream(out);
		return out.toByteArray();
	}

	@Override
	public WebzFile getParent() throws IOException, WebzException {

		String parentPathName = fileSystem.getParentPathName(pathName);
		if (parentPathName == null) {
			return null;
		}

		return fileFactory.get(parentPathName);
	}

	@Override
	public Collection<WebzFile> getChildren() throws IOException, WebzException {

		Collection<String> childPathNames = fileSystem.getChildPathNames(pathName);
		if (childPathNames == null) {
			return null;
		}

		Collection<WebzFile> children = new ArrayList<>(childPathNames.size());
		for (String childPathName : childPathNames) {
			children.add(fileFactory.get(childPathName));
		}
		return children;
	}

	@Override
	public WebzMetadata createFolder() throws IOException, WebzException {
		return fileSystem.createFolder(pathName);
	}

	@Override
	public WebzMetadata uploadFile(byte[] content) throws IOException, WebzException {
		return fileSystem.uploadFile(pathName, content);
	}

	@Override
	public WebzMetadata move(WebzFile destFile) throws IOException, WebzException {
		return move(destFile.getPathName());
	}

	@Override
	public WebzMetadata copy(WebzFile destFile) throws IOException, WebzException {
		return copy(destFile.getPathName());
	}

	@Override
	public WebzMetadata move(String destPathName) throws IOException, WebzException {
		return fileSystem.move(pathName, destPathName);
	}

	@Override
	public WebzMetadata copy(String destPathName) throws IOException, WebzException {
		return fileSystem.copy(pathName, destPathName);
	}

	@Override
	public void delete() throws IOException, WebzException {
		fileSystem.delete(pathName);
	}

}
