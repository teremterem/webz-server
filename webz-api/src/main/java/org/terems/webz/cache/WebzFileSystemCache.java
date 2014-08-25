package org.terems.webz.cache;

import java.util.Collection;
import java.util.Map;

import org.terems.webz.WebzFileSystem;
import org.terems.webz.WebzMetadata;

/** TODO !!! describe !!! **/
public interface WebzFileSystemCache {

	/** TODO !!! describe !!! **/
	public void init(WebzFileSystem innerFileSystem, int filePayloadSizeThreshold);

	/** TODO !!! describe !!! **/
	public String getCacheTypeName();

	/** TODO !!! describe !!! **/
	public void putMetadataIntoCache(String pathName, WebzMetadata metadata);

	/** TODO !!! describe !!! **/
	public WebzMetadata fetchMetadata(String pathName);

	/** TODO !!! describe !!! **/
	public Map<String, WebzMetadata> fetchMetadata(Collection<String> pathNames);

	/** TODO !!! describe !!! **/
	public ChildPathNamesHolder fetchChildPathNamesHolder(String parentPathName);

	/** TODO !!! describe !!! **/
	public FilePayloadHolder fetchFilePayloadHolder(String pathName);

	/** TODO !!! describe !!! **/
	public void dropMetadataFromCache(String pathName);

	/** TODO !!! describe !!! **/
	public void dropChildPathNamesHolderFromCache(String parentPathName);

	/** TODO !!! describe !!! **/
	public void dropFilePayloadHolderFromCache(String pathName);

}
