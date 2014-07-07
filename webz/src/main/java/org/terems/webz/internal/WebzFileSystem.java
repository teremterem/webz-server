package org.terems.webz.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFileFactory;
import org.terems.webz.WebzFileMetadata;

/** TODO !!! describe !!! */
public interface WebzFileSystem extends WebzFileFactory {

	/** TODO !!! describe !!! */
	public String _getFileSystemUniqueId();

	/** TODO !!! describe !!! */
	public WebzFileMetadata _getMetadata(String pathName) throws IOException, WebzException;

	/** TODO !!! describe !!! */
	public ParentChildrenMetadata _getParentChildrenMetadata(String parentPathName) throws IOException, WebzException;

	/** TODO !!! describe !!! */
	public FreshParentChildrenMetadata _getParentChildrenMetadataIfChanged(String parentPathName, Object previousFolderHash)
			throws IOException, WebzException;

	/** TODO !!! describe !!! */
	public Map<String, WebzFileMetadata> _getChildPathNamesAndMetadata(String parentPathName) throws IOException, WebzException;

	/** TODO !!! describe !!! */
	public Collection<String> _getChildPathNames(String parentPathName) throws IOException, WebzException;

	/** TODO !!! describe !!! */
	public WebzFileMetadata _fileContentToOutputStream(String pathName, OutputStream out) throws IOException, WebzException;

	/** TODO !!! describe !!! */
	public WebzFileDownloader _getFileContentDownloader(String pathName) throws IOException, WebzException;

	/** TODO !!! describe !!! */
	public WebzFileMetadata _createFolder(String pathName) throws IOException, WebzException;

	/** TODO !!! describe !!! */
	public WebzFileMetadata _uploadFile(String pathName, byte[] content) throws IOException, WebzException;

	/** TODO !!! describe !!! */
	public WebzFileMetadata _move(String srcPathName, String destPathName) throws IOException, WebzException;

	/** TODO !!! describe !!! */
	public WebzFileMetadata _copy(String srcPathName, String destPathName) throws IOException, WebzException;

	/** TODO !!! describe !!! */
	public void _delete(String pathName) throws IOException, WebzException;

}
