package org.terems.webz;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import org.terems.webz.cache.WebzFileSystemCache;

/** TODO !!! describe !!! **/
public interface WebzFileSystem extends WebzDestroyable {

	/** TODO !!! describe !!! **/
	public void init(Properties properties) throws WebzException;

	/** TODO !!! describe !!! **/
	public String getFileSystemUniqueId();

	/** TODO !!! describe !!! **/
	public String normalizePathname(String nonNormalizedPathname);

	/** TODO !!! describe !!! **/
	public boolean isNormalizedPathnameInvalid(String pathname);

	/** TODO !!! describe !!! **/
	public String getParentPathname(String pathname);

	/** TODO !!! describe !!! **/
	public String concatPathname(String basePathname, String relativePathname);

	/** TODO !!! describe !!! **/
	public boolean belongsToSubtree(String pathname, String subtreePathname);

	/** TODO !!! describe !!! **/
	public void inflate(WebzFile file) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public void inflate(WebzFileSystemCache fileSystemCache, WebzFile file) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata getMetadata(String pathname) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public ParentChildrenMetadata getParentChildrenMetadata(String parentPathname) throws IOException, WebzException;

	/**
	 * @return null if folder hash has not changed, otherwise - FreshParentChildrenMetadata object that encapsulates ParentChildrenMetadata;
	 *         however encapsulated ParentChildrenMetadata may be null if there is no such file or folder...
	 **/
	public FreshParentChildrenMetadata getParentChildrenMetadataIfChanged(String parentPathname, Object previousFolderHash)
			throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public Map<String, WebzMetadata> getChildPathnamesAndMetadata(String parentPathname) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public Collection<String> getChildPathnames(String parentPathname) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata.FileSpecific copyContentToOutputStream(String pathname, OutputStream out) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzFileDownloader getFileDownloader(String pathname) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata createFolder(String pathname) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata.FileSpecific uploadFile(String pathname, InputStream content, long numBytes) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata.FileSpecific uploadFile(String pathname, InputStream content) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata move(String srcPathname, String destPathname) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzMetadata copy(String srcPathname, String destPathname) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public void delete(String pathname) throws IOException, WebzException;

}
