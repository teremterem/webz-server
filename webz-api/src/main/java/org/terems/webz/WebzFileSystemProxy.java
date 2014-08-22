package org.terems.webz;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

/** TODO !!! describe !!! **/
public abstract class WebzFileSystemProxy implements WebzFileSystem {

	/** TODO !!! describe !!! **/
	protected abstract WebzFileSystem getInnerFileSystem();

	/** TODO !!! describe !!! **/
	@Override
	public String getFileSystemUniqueId() {
		return getInnerFileSystem().getFileSystemUniqueId();
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzMetadata getMetadata(String pathName) throws IOException, WebzException {
		return getInnerFileSystem().getMetadata(pathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public String getParentPathName(String pathName) throws IOException, WebzException {
		return getInnerFileSystem().getParentPathName(pathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public ParentChildrenMetadata getParentChildrenMetadata(String parentPathName) throws IOException, WebzException {
		return getInnerFileSystem().getParentChildrenMetadata(parentPathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public FreshParentChildrenMetadata getParentChildrenMetadataIfChanged(String parentPathName, Object previousFolderHash)
			throws IOException, WebzException {
		return getInnerFileSystem().getParentChildrenMetadataIfChanged(parentPathName, previousFolderHash);
	}

	/** TODO !!! describe !!! **/
	@Override
	public Map<String, WebzMetadata> getChildPathNamesAndMetadata(String parentPathName) throws IOException, WebzException {
		return getInnerFileSystem().getChildPathNamesAndMetadata(parentPathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public Collection<String> getChildPathNames(String parentPathName) throws IOException, WebzException {
		return getInnerFileSystem().getChildPathNames(parentPathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzMetadata.FileSpecific fileContentToOutputStream(String pathName, OutputStream out) throws IOException, WebzException {
		return getInnerFileSystem().fileContentToOutputStream(pathName, out);
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzFileDownloader getFileContentDownloader(String pathName) throws IOException, WebzException {
		return getInnerFileSystem().getFileContentDownloader(pathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzMetadata createFolder(String pathName) throws IOException, WebzException {
		return getInnerFileSystem().createFolder(pathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzMetadata.FileSpecific uploadFile(String pathName, byte[] content) throws IOException, WebzException {
		return getInnerFileSystem().uploadFile(pathName, content);
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzMetadata move(String srcPathName, String destPathName) throws IOException, WebzException {
		return getInnerFileSystem().move(srcPathName, destPathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzMetadata copy(String srcPathName, String destPathName) throws IOException, WebzException {
		return getInnerFileSystem().copy(srcPathName, destPathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public void delete(String pathName) throws IOException, WebzException {
		getInnerFileSystem().delete(pathName);
	}

}
