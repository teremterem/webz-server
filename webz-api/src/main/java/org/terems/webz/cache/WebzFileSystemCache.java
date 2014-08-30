package org.terems.webz.cache;

import java.util.Collection;
import java.util.Map;

import org.terems.webz.WebzDestroyable;
import org.terems.webz.WebzFileSystem;
import org.terems.webz.WebzMetadata;

/** TODO !!! describe !!! **/
public interface WebzFileSystemCache extends WebzDestroyable {

	/** TODO !!! describe !!! **/
	public void init(WebzFileSystem innerFileSystem, int filePayloadSizeThreshold);

	/** TODO !!! describe !!! **/
	public String getCacheTypeName();

	/** TODO !!! describe !!! **/
	public WebzMetadata fetchMetadata(String pathName);

	/** TODO !!! describe !!! **/
	public Map<String, WebzMetadata> fetchMetadata(Collection<String> pathNames);

	/** TODO !!! describe !!! **/
	public ChildPathNamesHolder fetchChildPathNamesHolder(String parentPathName);

	/** TODO !!! describe !!! **/
	public FileContentHolder fetchFileContentHolder(String pathName);

	/** TODO !!! describe !!! **/
	public void putMetadataIntoCache(String pathName, WebzMetadata metadata);

	/** TODO !!! describe !!! **/
	public void putChildPathNamesHolderIntoCache(String pathName, ChildPathNamesHolder childPathNamesHolder);

	/** TODO !!! describe !!! **/
	public void putFileContentHolderIntoCache(String pathName, FileContentHolder fileContentHolder);

	/** TODO !!! describe !!! **/
	public void dropMetadataFromCache(String pathName);

	/** TODO !!! describe !!! **/
	public void dropChildPathNamesHolderFromCache(String parentPathName);

	/** TODO !!! describe !!! **/
	public void dropFileContentHolderFromCache(String pathName);

}
