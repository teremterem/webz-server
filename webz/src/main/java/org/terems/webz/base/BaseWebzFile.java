package org.terems.webz.base;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

import org.terems.webz.WebzConstants;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileMetadata;
import org.terems.webz.WebzFileSystem;

public class BaseWebzFile extends WebzFileMetadataProxy<Object> implements WebzFile {

	public static String trimFileSeparators(String pathName) {
		pathName = pathName.trim();
		if (pathName.startsWith("/") || pathName.startsWith("\\")) {
			pathName = pathName.substring(1);
		}
		if (pathName.endsWith("/") || pathName.endsWith("\\")) {
			pathName = pathName.substring(0, pathName.length() - 1);
		}
		return pathName;
	}

	private WebzFileSystem fileSystem;
	private String pathName;
	private AtomicReference<WebzFileMetadata<Object>> metadataRef;

	public BaseWebzFile(WebzFileSystem fileSystem, String pathName) {
		this.fileSystem = fileSystem;
		this.pathName = trimFileSeparators(pathName);
	}

	@Override
	public String getPathName() {
		return pathName;
	}

	@Override
	public byte[] getFileContent(long expectedNumberOfBytes) throws IOException, WebzException {
		ByteArrayOutputStream out = new ByteArrayOutputStream((int) expectedNumberOfBytes);
		fileContentToOutputStream(out);
		return out.toByteArray();
	}

	@Override
	public byte[] getFileContent() throws IOException, WebzException {
		FileSpecific fileSpecific = getFileSpecific();
		return getFileContent(fileSpecific == null ? WebzConstants.DEFAULT_BUF_SIZE : fileSpecific.getNumberOfBytes());
	}

	@Override
	public boolean exits() throws IOException, WebzException {
		return getMetadata() != null;
	}

	@Override
	public synchronized void setMetadataThreadSafe(WebzFileMetadata<Object> metadata) {
		if (this.metadataRef == null) {
			this.metadataRef = new AtomicReference<WebzFileMetadata<Object>>(metadata);
		} else {
			this.metadataRef.set(metadata);
		}
	}

	protected synchronized void initMetadataThreadSafe() throws IOException, WebzException {
		if (!isMetadataInitialized()) {
			fileSystem._fetchMetadata(this);
		}
	}

	// ~

	@Override
	public boolean isMetadataInitialized() {
		return metadataRef != null;
	}

	@Override
	public synchronized void refreshThreadSafe() {
		metadataRef = null;
	}

	// ~

	@Override
	protected WebzFileMetadata<Object> getMetadata() throws IOException, WebzException {
		if (!isMetadataInitialized()) {
			initMetadataThreadSafe();
		}
		return metadataRef.get();
	}

	@Override
	public WebzFile fileContentToOutputStream(OutputStream out) throws IOException, WebzException {
		return fileSystem._fileContentToOutputStream(this, out);
	}

	// ~

	@Override
	public WebzFile createFolder() throws IOException, WebzException {
		return fileSystem._createFolder(this);
	}

	@Override
	public WebzFile uploadFile(byte[] content) throws IOException, WebzException {
		return fileSystem._uploadFile(this, content);
	}

	@Override
	public WebzFile move(WebzFile destFile) throws IOException, WebzException {
		return fileSystem._move(this, destFile);
	}

	@Override
	public WebzFile copy(WebzFile destFile) throws IOException, WebzException {
		return fileSystem._copy(this, destFile);
	}

	@Override
	public WebzFile move(String destPathName) throws IOException, WebzException {
		return move(fileSystem.get(destPathName));
	}

	@Override
	public WebzFile copy(String destPathName) throws IOException, WebzException {
		return copy(fileSystem.get(destPathName));
	}

	@Override
	public void delete() throws IOException, WebzException {
		fileSystem._delete(this);
	}

}
