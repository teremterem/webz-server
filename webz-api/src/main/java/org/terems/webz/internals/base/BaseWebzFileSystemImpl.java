package org.terems.webz.internals.base;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzMetadata;
import org.terems.webz.WebzProperties;
import org.terems.webz.base.BaseWebzPropertiesInitable;
import org.terems.webz.internals.FreshParentChildrenMetadata;
import org.terems.webz.internals.ParentChildrenMetadata;
import org.terems.webz.internals.WebzFileSystemCache;
import org.terems.webz.internals.WebzFileSystemImpl;
import org.terems.webz.internals.WebzFileSystemStructure;
import org.terems.webz.internals.WebzPathNormalizer;

/**
 * Basic implementation of {@code WebzFileSystemImpl} to be extended by concrete implementations...
 **/
public abstract class BaseWebzFileSystemImpl extends BaseWebzPropertiesInitable implements WebzFileSystemImpl {

	protected WebzPathNormalizer pathNormalizer;
	protected WebzFileSystemStructure structure = this;
	protected String uniqueId;

	@Override
	public BaseWebzFileSystemImpl init(WebzPathNormalizer pathNormalizer, WebzProperties properties) throws WebzException {

		setPathNormalizer(pathNormalizer);
		init(properties);
		return this;
	}

	@Override
	public void setPathNormalizer(WebzPathNormalizer pathNormalizer) {
		this.pathNormalizer = pathNormalizer;
	}

	@Override
	public void setFileSystemStructure(WebzFileSystemStructure structure) {
		this.structure = structure;
	}

	@Override
	public String getUniqueId() {
		return uniqueId;
	}

	@Override
	public void inflate(WebzFile file) throws IOException, WebzException {
	}

	/** Do nothing by default... **/
	@Override
	public void inflate(WebzFileSystemCache fileSystemCache, WebzFile file) throws IOException, WebzException {
	}

	/** Default implementation... **/
	@Override
	public FreshParentChildrenMetadata getParentChildrenMetadataIfChanged(String parentPathname, Object previousFolderHash)
			throws IOException, WebzException {

		ParentChildrenMetadata parentChildrenMetadata = getParentChildrenMetadata(parentPathname);
		if (previousFolderHash != null && parentChildrenMetadata != null && previousFolderHash.equals(parentChildrenMetadata.folderHash)) {
			return null;
		}

		return new FreshParentChildrenMetadata(parentChildrenMetadata);
	}

	/** Default implementation... **/
	@Override
	public Map<String, WebzMetadata> getChildPathnamesAndMetadata(String parentPathname) throws IOException, WebzException {
		ParentChildrenMetadata parentChildrenMetadata = getParentChildrenMetadata(parentPathname);
		return parentChildrenMetadata == null ? null : parentChildrenMetadata.childPathnamesAndMetadata;
	}

	/** Default implementation... **/
	@Override
	public Collection<String> getChildPathnames(String parentPathname) throws IOException, WebzException {

		ParentChildrenMetadata parentChildrenMetadata = getParentChildrenMetadata(parentPathname);
		if (parentChildrenMetadata == null || parentChildrenMetadata.childPathnamesAndMetadata == null) {
			return null;
		}

		return parentChildrenMetadata.childPathnamesAndMetadata.keySet();
	}

	/** Default implementation... **/
	@Override
	public WebzMetadata.FileSpecific copyContentToOutputStream(String pathname, OutputStream out) throws IOException, WebzException {

		WebzFileDownloader downloader = getFileDownloader(pathname);
		if (downloader == null) {
			return null;
		}

		downloader.copyContentAndClose(out);

		return downloader.fileSpecific;
	}

}
