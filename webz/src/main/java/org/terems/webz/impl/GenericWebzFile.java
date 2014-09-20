package org.terems.webz.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzFileFactory;
import org.terems.webz.WebzFileSystem;
import org.terems.webz.WebzMetadata;
import org.terems.webz.WebzPathNameException;

public class GenericWebzFile implements WebzFile {

	protected final String pathName;
	protected WebzFileFactory fileFactory;
	protected WebzFileSystem fileSystem;

	private Boolean pathNameInvalid = null;
	private boolean inflated = false;

	public GenericWebzFile(String pathName, WebzFileFactory fileFactory, WebzFileSystem fileSystem) {
		this.pathName = fileSystem.normalizePathName(pathName);
		this.fileFactory = fileFactory;
		this.fileSystem = fileSystem;
	}

	@Override
	public String getPathName() {
		return pathName;
	}

	@Override
	public boolean isPathNameInvalid() {

		if (pathNameInvalid == null) {
			pathNameInvalid = fileSystem.isNormalizedPathNameInvalid(this.pathName);
		}
		return pathNameInvalid;
	}

	@Override
	public WebzFile getParent() throws WebzPathNameException {

		assertPathNameNotInvalid();

		String parentPathName = fileSystem.getParentPathName(pathName);
		if (parentPathName == null) {
			return null;
		}

		return fileFactory.get(parentPathName);
	}

	@Override
	public WebzFile getDescendant(String relativePathName) throws WebzPathNameException {

		assertPathNameNotInvalid();

		return fileFactory.get(fileSystem.concatPathName(pathName, fileSystem.normalizePathName(relativePathName)));
	}

	@Override
	public void inflate() throws IOException, WebzException {

		if (!inflated) {

			if (!isPathNameInvalid()) {
				fileSystem.inflate(this);
			}
			inflated = true;
		}
	}

	@Override
	public WebzMetadata getMetadata() throws IOException, WebzException {

		if (isPathNameInvalid()) {
			return null;
		}

		return fileSystem.getMetadata(pathName);
	}

	@Override
	public WebzFileDownloader getFileDownloader() throws IOException, WebzException {

		if (isPathNameInvalid()) {
			return null;
		}

		return fileSystem.getFileDownloader(pathName);
	}

	@Override
	public WebzMetadata copyContentToOutputStream(OutputStream out) throws IOException, WebzException {

		if (isPathNameInvalid()) {
			return null;
		}

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
	public Collection<WebzFile> listChildren() throws IOException, WebzException {

		if (isPathNameInvalid()) {
			return null;
		}

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

		assertPathNameNotInvalid();

		return fileSystem.createFolder(pathName);
	}

	@Override
	public WebzMetadata uploadFile(InputStream content, long numBytes) throws IOException, WebzException {

		assertPathNameNotInvalid();

		return fileSystem.uploadFile(pathName, content, numBytes);
	}

	@Override
	public WebzMetadata uploadFile(InputStream content) throws IOException, WebzException {

		assertPathNameNotInvalid();

		return fileSystem.uploadFile(pathName, content);
	}

	@Override
	public WebzMetadata uploadFile(byte[] content) throws IOException, WebzException {
		return uploadFile(new ByteArrayInputStream(content), content.length);
	}

	@Override
	public WebzMetadata move(WebzFile destFile) throws IOException, WebzException {

		assertPathNameNotInvalid();
		assertPathNameNotInvalid(destFile);

		return fileSystem.move(pathName, destFile.getPathName());
	}

	@Override
	public WebzMetadata copy(WebzFile destFile) throws IOException, WebzException {

		assertPathNameNotInvalid();
		assertPathNameNotInvalid(destFile);

		return fileSystem.copy(pathName, destFile.getPathName());
	}

	@Override
	public WebzMetadata move(String destPathName) throws IOException, WebzException {
		return move(fileFactory.get(destPathName));
	}

	@Override
	public WebzMetadata copy(String destPathName) throws IOException, WebzException {
		return copy(fileFactory.get(destPathName));
	}

	@Override
	public void delete() throws IOException, WebzException {

		assertPathNameNotInvalid();

		fileSystem.delete(pathName);
	}

	protected void assertPathNameNotInvalid() throws WebzPathNameException {
		assertPathNameNotInvalid(this);
	}

	protected static void assertPathNameNotInvalid(WebzFile file) throws WebzPathNameException {

		if (file.isPathNameInvalid()) {

			throw new WebzPathNameException("'" + file.getPathName() + "' is not a valid path name");
		}
	}

}
