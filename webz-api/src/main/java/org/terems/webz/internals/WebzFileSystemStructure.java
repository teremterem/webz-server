package org.terems.webz.internals;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzMetadata;

/** TODO !!! describe !!! **/
public interface WebzFileSystemStructure extends WebzPathNormalizerSettable {

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

}
