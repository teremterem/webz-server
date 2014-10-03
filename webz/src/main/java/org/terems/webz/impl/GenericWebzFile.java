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
import org.terems.webz.WebzMetadata;
import org.terems.webz.WebzPathnameException;
import org.terems.webz.internals.WebzFileFactory;
import org.terems.webz.internals.WebzFileSystem;
import org.terems.webz.util.WebzUtils;

public class GenericWebzFile implements WebzFile {

	protected final String pathname;
	protected WebzFileSystem fileSystem;

	private Boolean pathnameInvalid = null;
	private boolean inflated = false;

	public GenericWebzFile(String pathname, WebzFileSystem fileSystem) {
		this.pathname = fileSystem.getPathNormalizer().normalizePathname(pathname);
		this.fileSystem = fileSystem;
	}

	@Override
	public String getPathname() {
		return pathname;
	}

	@Override
	public boolean isPathnameInvalid() {

		if (pathnameInvalid == null) {
			pathnameInvalid = fileSystem.getPathNormalizer().isNormalizedPathnameInvalid(this.pathname);
		}
		return pathnameInvalid;
	}

	@Override
	public WebzFile getParent() throws WebzPathnameException {

		assertPathnameNotInvalid();

		String parentPathname = fileSystem.getPathNormalizer().getParentPathname(pathname);
		if (parentPathname == null) {
			return null;
		}

		return fileSystem.getFileFactory().get(parentPathname);
	}

	@Override
	public WebzFile getDescendant(String relativePathname) throws WebzPathnameException {

		assertPathnameNotInvalid();

		return fileSystem.getFileFactory()
				.get(fileSystem.getPathNormalizer().concatPathname(pathname,
						fileSystem.getPathNormalizer().normalizePathname(relativePathname)));
	}

	@Override
	public boolean belongsToSubtree(WebzFile subtree) throws WebzPathnameException {

		assertPathnameNotInvalid();
		assertPathnameNotInvalid(subtree);

		return fileSystem.getPathNormalizer().belongsToSubtree(pathname, subtree.getPathname());
	}

	@Override
	public boolean belongsToSubtree(String subtreePath) throws WebzPathnameException {
		return belongsToSubtree(fileSystem.getFileFactory().get(subtreePath));
	}

	@Override
	public void inflate() throws IOException, WebzException {

		if (!inflated) {

			if (!isPathnameInvalid()) {
				fileSystem.getStructure().inflate(this);
			}
			inflated = true;
		}
	}

	@Override
	public WebzMetadata getMetadata() throws IOException, WebzException {

		if (isPathnameInvalid()) {
			return null;
		}

		return fileSystem.getStructure().getMetadata(pathname);
	}

	@Override
	public WebzFileDownloader getFileDownloader() throws IOException, WebzException {

		if (isPathnameInvalid()) {
			return null;
		}

		return fileSystem.getOperations().getFileDownloader(pathname);
	}

	@Override
	public WebzMetadata.FileSpecific copyContentToOutputStream(OutputStream out) throws IOException, WebzException {

		if (isPathnameInvalid()) {
			return null;
		}

		return fileSystem.getOperations().copyContentToOutputStream(pathname, out);
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
			throw new IndexOutOfBoundsException(WebzUtils.formatFileSystemMessage(this + " (" + numBytes
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

		Collection<String> childPathnames = fileSystem.getStructure().getChildPathnames(pathname);
		if (childPathnames == null) {
			return null;
		}

		WebzFileFactory fileFactory = fileSystem.getFileFactory();
		Collection<WebzFile> children = new ArrayList<>(childPathnames.size());
		for (String childPathname : childPathnames) {
			children.add(fileFactory.get(childPathname));
		}
		return children;
	}

	@Override
	public WebzMetadata createFolder() throws IOException, WebzException {

		assertPathnameNotInvalid();

		return fileSystem.getOperations().createFolder(pathname);
	}

	@Override
	public WebzMetadata uploadFile(InputStream content, long numBytes) throws IOException, WebzException {

		assertPathnameNotInvalid();

		return fileSystem.getOperations().uploadFile(pathname, content, numBytes);
	}

	@Override
	public WebzMetadata uploadFile(InputStream content) throws IOException, WebzException {

		assertPathnameNotInvalid();

		return fileSystem.getOperations().uploadFile(pathname, content);
	}

	@Override
	public WebzMetadata uploadFile(byte[] content) throws IOException, WebzException {
		return uploadFile(new ByteArrayInputStream(content), content.length);
	}

	@Override
	public WebzMetadata move(WebzFile destFile) throws IOException, WebzException {

		assertPathnameNotInvalid();
		assertPathnameNotInvalid(destFile);

		return fileSystem.getOperations().move(pathname, destFile.getPathname());
	}

	@Override
	public WebzMetadata copy(WebzFile destFile) throws IOException, WebzException {

		assertPathnameNotInvalid();
		assertPathnameNotInvalid(destFile);

		return fileSystem.getOperations().copy(pathname, destFile.getPathname());
	}

	@Override
	public WebzMetadata move(String destPathname) throws IOException, WebzException {
		return move(fileSystem.getFileFactory().get(destPathname));
	}

	@Override
	public WebzMetadata copy(String destPathname) throws IOException, WebzException {
		return copy(fileSystem.getFileFactory().get(destPathname));
	}

	@Override
	public void delete() throws IOException, WebzException {

		assertPathnameNotInvalid();

		fileSystem.getOperations().delete(pathname);
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
		return WebzUtils.formatFileSystemMessage(getClass().getSimpleName() + " '" + pathname + "'", fileSystem);
	}

}
