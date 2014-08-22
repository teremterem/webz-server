package org.terems.webz;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

/** TODO !!! describe !!! **/
public interface WebzFileSystem {

	/** TODO !!! describe !!! **/
	public String getFileSystemUniqueId();

	/** TODO !!! describe !!! **/
	public WebzMetadata getMetadata(String pathName) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public String getParentPathName(String pathName) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public ParentChildrenMetadata getParentChildrenMetadata(String parentPathName) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public FreshParentChildrenMetadata getParentChildrenMetadataIfChanged(String parentPathName, Object previousFolderHash)
			throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public Map<String, WebzMetadata> getChildPathNamesAndMetadata(String parentPathName) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public Collection<String> getChildPathNames(String parentPathName) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata.FileSpecific fileContentToOutputStream(String pathName, OutputStream out) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzFileDownloader getFileContentDownloader(String pathName) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata createFolder(String pathName) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata.FileSpecific uploadFile(String pathName, byte[] content) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata move(String srcPathName, String destPathName) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata copy(String srcPathName, String destPathName) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public void delete(String pathName) throws IOException, WebzException;

}
