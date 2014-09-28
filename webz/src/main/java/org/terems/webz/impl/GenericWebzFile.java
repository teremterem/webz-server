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
import org.terems.webz.WebzPathnameException;
import org.terems.webz.util.WebzUtils;

public class GenericWebzFile implements WebzFile {

	protected final String pathname;
	protected WebzFileFactory fileFactory;
	protected WebzFileSystem fileSystem;

	private Boolean pathnameInvalid = null;
	private boolean inflated = false;

	public GenericWebzFile(String pathname, WebzFileFactory fileFactory, WebzFileSystem fileSystem) {
		this.pathname = fileSystem.normalizePathname(pathname);
		this.fileFactory = fileFactory;
		this.fileSystem = fileSystem;
	}

	@Override
	public String getPathname() {
		return pathname;
	}

	@Override
	public boolean isPathnameInvalid() {

		if (pathnameInvalid == null) {
			pathnameInvalid = fileSystem.isNormalizedPathnameInvalid(this.pathname);
		}
		return pathnameInvalid;
	}

	@Override
	public WebzFile getParent() throws WebzPathnameException {

		assertPathnameNotInvalid();

		String parentPathname = fileSystem.getParentPathname(pathname);
		if (parentPathname == null) {
			return null;
		}

		return fileFactory.get(parentPathname);
	}

	@Override
	public WebzFile getDescendant(String relativePathname) throws WebzPathnameException {

		assertPathnameNotInvalid();

		return fileFactory.get(fileSystem.concatPathname(pathname, fileSystem.normalizePathname(relativePathname)));
	}

	@Override
	public boolean belongsToSubtree(WebzFile subtree) throws WebzPathnameException {

		assertPathnameNotInvalid();
		assertPathnameNotInvalid(subtree);

		return fileSystem.belongsToSubtree(pathname, subtree.getPathname());
	}

	@Override
	public boolean belongsToSubtree(String subtreePath) throws WebzPathnameException {
		return belongsToSubtree(fileFactory.get(subtreePath));
	}

	@Override
	public void inflate() throws IOException, WebzException {

		if (!inflated) {

			if (!isPathnameInvalid()) {
				fileSystem.inflate(this);
			}
			inflated = true;
		}
	}

	@Override
	public WebzMetadata getMetadata() throws IOException, WebzException {

		if (isPathnameInvalid()) {
			return null;
		}

		return fileSystem.getMetadata(pathname);
	}

	@Override
	public WebzFileDownloader getFileDownloader() throws IOException, WebzException {

		if (isPathnameInvalid()) {
			return null;
		}

		return fileSystem.getFileDownloader(pathname);
	}

	@Override
	public WebzMetadata.FileSpecific copyContentToOutputStream(OutputStream out) throws IOException, WebzException {

		if (isPathnameInvalid()) {
			return null;
		}

		return fileSystem.copyContentToOutputStream(pathname, out);
	}

	@Override
	public byte[] getFileContent() throws IOException, WebzException {

		WebzFileDownloader downloader = getFileDownloader();
		if (downloader == null) {
			return null;
		}
		WebzMetadata.FileSpecific fileSpecific = downloader.fileSpecific;

		long numBytes = fileSpecific.getNumberOfBytes();
		if (numBytes > Integer.MAX_VALUE) {
			throw new IndexOutOfBoundsException(WebzUtils.formatFileSystemMessage("file '" + pathname + "' (" + numBytes
					+ " bytes) is too big for a byte array", fileSystem));
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream((int) numBytes);
		downloader.copyContentAndClose(out);
		return out.toByteArray();
	}

	@Override
	public Collection<WebzFile> listChildren() throws IOException, WebzException {

		if (isPathnameInvalid()) {
			return null;
		}

		Collection<String> childPathnames = fileSystem.getChildPathnames(pathname);
		if (childPathnames == null) {
			return null;
		}

		Collection<WebzFile> children = new ArrayList<>(childPathnames.size());
		for (String childPathname : childPathnames) {
			children.add(fileFactory.get(childPathname));
		}
		return children;
	}

	@Override
	public WebzMetadata createFolder() throws IOException, WebzException {

		assertPathnameNotInvalid();

		return fileSystem.createFolder(pathname);
	}

	@Override
	public WebzMetadata uploadFile(InputStream content, long numBytes) throws IOException, WebzException {

		assertPathnameNotInvalid();

		return fileSystem.uploadFile(pathname, content, numBytes);
	}

	@Override
	public WebzMetadata uploadFile(InputStream content) throws IOException, WebzException {

		assertPathnameNotInvalid();

		return fileSystem.uploadFile(pathname, content);
	}

	@Override
	public WebzMetadata uploadFile(byte[] content) throws IOException, WebzException {
		return uploadFile(new ByteArrayInputStream(content), content.length);
	}

	@Override
	public WebzMetadata move(WebzFile destFile) throws IOException, WebzException {

		assertPathnameNotInvalid();
		assertPathnameNotInvalid(destFile);

		return fileSystem.move(pathname, destFile.getPathname());
	}

	@Override
	public WebzMetadata copy(WebzFile destFile) throws IOException, WebzException {

		assertPathnameNotInvalid();
		assertPathnameNotInvalid(destFile);

		return fileSystem.copy(pathname, destFile.getPathname());
	}

	@Override
	public WebzMetadata move(String destPathname) throws IOException, WebzException {
		return move(fileFactory.get(destPathname));
	}

	@Override
	public WebzMetadata copy(String destPathname) throws IOException, WebzException {
		return copy(fileFactory.get(destPathname));
	}

	@Override
	public void delete() throws IOException, WebzException {

		assertPathnameNotInvalid();

		fileSystem.delete(pathname);
	}

	protected void assertPathnameNotInvalid() throws WebzPathnameException {
		assertPathnameNotInvalid(this);
	}

	protected static void assertPathnameNotInvalid(WebzFile file) throws WebzPathnameException {

		if (file.isPathnameInvalid()) {

			throw new WebzPathnameException("'" + file.getPathname() + "' is not a valid pathname");
		}
	}

	@Override
	public String toString() {
		return WebzUtils.formatFileSystemMessage("'" + pathname + "' - " + getClass().getSimpleName(), fileSystem);
	}

}
