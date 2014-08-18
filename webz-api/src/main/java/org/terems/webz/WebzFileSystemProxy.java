package org.terems.webz;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

/** TODO !!! describe !!! **/
public abstract class WebzFileSystemProxy implements WebzFileSystem {

	/** TODO !!! describe !!! **/
	protected abstract WebzFileSystem getFileSystem();

	/** TODO !!! describe !!! **/
	@Override
	public String getFileSystemUniqueId() {
		return getFileSystem().getFileSystemUniqueId();
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzFileMetadata getMetadata(String pathName) throws IOException, WebzException {
		return getFileSystem().getMetadata(pathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public ParentChildrenMetadata getParentChildrenMetadata(String parentPathName) throws IOException, WebzException {
		return getFileSystem().getParentChildrenMetadata(parentPathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public FreshParentChildrenMetadata getParentChildrenMetadataIfChanged(String parentPathName, Object previousFolderHash)
			throws IOException, WebzException {
		return getFileSystem().getParentChildrenMetadataIfChanged(parentPathName, previousFolderHash);
	}

	/** TODO !!! describe !!! **/
	@Override
	public Map<String, WebzFileMetadata> getChildPathNamesAndMetadata(String parentPathName) throws IOException, WebzException {
		return getFileSystem().getChildPathNamesAndMetadata(parentPathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public Collection<String> getChildPathNames(String parentPathName) throws IOException, WebzException {
		return getFileSystem().getChildPathNames(parentPathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzFileMetadata.FileSpecific fileContentToOutputStream(String pathName, OutputStream out) throws IOException, WebzException {
		return getFileSystem().fileContentToOutputStream(pathName, out);
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzFileDownloader getFileContentDownloader(String pathName) throws IOException, WebzException {
		return getFileSystem().getFileContentDownloader(pathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzFileMetadata createFolder(String pathName) throws IOException, WebzException {
		return getFileSystem().createFolder(pathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzFileMetadata.FileSpecific uploadFile(String pathName, byte[] content) throws IOException, WebzException {
		return getFileSystem().uploadFile(pathName, content);
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzFileMetadata move(String srcPathName, String destPathName) throws IOException, WebzException {
		return getFileSystem().move(srcPathName, destPathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzFileMetadata copy(String srcPathName, String destPathName) throws IOException, WebzException {
		return getFileSystem().copy(srcPathName, destPathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public void delete(String pathName) throws IOException, WebzException {
		getFileSystem().delete(pathName);
	}

}
