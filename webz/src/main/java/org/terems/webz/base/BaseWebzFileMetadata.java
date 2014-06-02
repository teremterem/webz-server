package org.terems.webz.base;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileMetadata;
import org.terems.webz.WebzFileSystem;

public abstract class BaseWebzFileMetadata<MO> implements WebzFileMetadata<MO> {

	public static FolderSpecific newFolderSpecific(final String hash, final List<WebzFile> children) throws IOException,
			WebzException {
		return new FolderSpecific() {

			@Override
			public String getHash() {
				return hash;
			}

			@Override
			public List<WebzFile> getChildren() {
				return children;
			}
		};
	}

	private final Object fileSpecificMutex = new Object();
	private final Object folderSpecificMutex = new Object();

	private WebzFileSystem fileSystem;
	private WebzFile file;
	private AtomicReference<MO> dbxMetaObjRef;
	private AtomicReference<FileSpecific> fileSpecificRef;
	private AtomicReference<FolderSpecific> folderSpecificRef;

	protected abstract FileSpecific initFileSpecific() throws IOException, WebzException;

	protected BaseWebzFileMetadata(WebzFileSystem fileSystem, WebzFile file, MO metadataObject) {
		this.fileSystem = fileSystem;
		this.file = file;
		dbxMetaObjRef = new AtomicReference<>(metadataObject);
	}

	protected MO getMetadataObject() {
		return dbxMetaObjRef.get();
	}

	@Override
	public void setMetadataObjectThreadSafe(MO metadataObject) {
		dbxMetaObjRef.set(metadataObject);
	}

	private void initFileSpecificThreadSafe() throws IOException, WebzException {
		synchronized (fileSpecificMutex) {
			if (fileSpecificRef == null) {
				fileSpecificRef = new AtomicReference<>(initFileSpecific());
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected WebzFileMetadata.FolderSpecific fetchWithChildrenAndInitFolderSpecific() throws IOException, WebzException {
		return fileSystem.fetchMetadataWithChildren(file, (WebzFileMetadata<Object>) this);
	}

	private void initFolderSpecificThreadSafe() throws IOException, WebzException {
		synchronized (folderSpecificMutex) {
			if (folderSpecificRef == null) {
				folderSpecificRef = new AtomicReference<>(fetchWithChildrenAndInitFolderSpecific());
			}
		}
	}

	@Override
	public FileSpecific getFileSpecific() throws IOException, WebzException {
		if (fileSpecificRef == null) {
			initFileSpecificThreadSafe();
		}
		return fileSpecificRef.get();
	}

	@Override
	public FolderSpecific getFolderSpecific() throws IOException, WebzException {
		if (folderSpecificRef == null) {
			initFolderSpecificThreadSafe();
		}
		return folderSpecificRef.get();
	}

}
