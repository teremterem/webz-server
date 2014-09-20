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
	public WebzMetadata fetchMetadata(String pathname);

	/** TODO !!! describe !!! **/
	public Map<String, WebzMetadata> fetchMetadata(Collection<String> pathnames);

	/** TODO !!! describe !!! **/
	public ChildPathnamesHolder fetchChildPathnamesHolder(String parentPathname);

	/** TODO !!! describe !!! **/
	public FileContentHolder fetchFileContentHolder(String pathname);

	/** TODO !!! describe !!! **/
	public void putMetadataIntoCache(String pathname, WebzMetadata metadata);

	/** TODO !!! describe !!! **/
	public void putChildPathnamesHolderIntoCache(String pathname, ChildPathnamesHolder childPathnamesHolder);

	/** TODO !!! describe !!! **/
	public void putFileContentHolderIntoCache(String pathname, FileContentHolder fileContentHolder);

	/** TODO !!! describe !!! **/
	public void dropMetadataFromCache(String pathname);

	/** TODO !!! describe !!! **/
	public void dropChildPathnamesHolderFromCache(String parentPathname);

	/** TODO !!! describe !!! **/
	public void dropFileContentHolderFromCache(String pathname);

}
