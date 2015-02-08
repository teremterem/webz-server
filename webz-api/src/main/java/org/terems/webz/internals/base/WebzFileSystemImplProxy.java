package org.terems.webz.internals.base;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzMetadata;
import org.terems.webz.WebzMetadata.FileSpecific;
import org.terems.webz.WebzProperties;
import org.terems.webz.base.BaseWebzDestroyable;
import org.terems.webz.internals.FreshParentChildrenMetadata;
import org.terems.webz.internals.ParentChildrenMetadata;
import org.terems.webz.internals.WebzFileSystemCache;
import org.terems.webz.internals.WebzFileSystemImpl;
import org.terems.webz.internals.WebzObjectFactory;
import org.terems.webz.internals.WebzPathNormalizer;

/** TODO !!! describe !!! **/
public abstract class WebzFileSystemImplProxy extends BaseWebzDestroyable implements WebzFileSystemImpl {

	/** TODO !!! describe !!! **/
	protected abstract WebzFileSystemImpl getInnerFileSystemImpl();

	@Override
	public void init(WebzPathNormalizer pathNormalizer, WebzProperties properties, WebzObjectFactory factory) throws WebzException {
		getInnerFileSystemImpl().init(pathNormalizer, properties, factory);
	}

	@Override
	public String getUniqueId() {
		return getInnerFileSystemImpl().getUniqueId();
	}

	@Override
	public void inflate(WebzFile file) throws IOException, WebzException {
		getInnerFileSystemImpl().inflate(file);
	}

	@Override
	public void inflate(WebzFileSystemCache fsCache, WebzFile file) throws IOException, WebzException {
		getInnerFileSystemImpl().inflate(fsCache, file);
	}

	@Override
	public WebzMetadata getMetadata(String pathname) throws IOException, WebzException {
		return getInnerFileSystemImpl().getMetadata(pathname);
	}

	@Override
	public ParentChildrenMetadata getParentChildrenMetadata(String parentPathname) throws IOException, WebzException {
		return getInnerFileSystemImpl().getParentChildrenMetadata(parentPathname);
	}

	@Override
	public FreshParentChildrenMetadata getParentChildrenMetadataIfChanged(String parentPathname, Object previousFolderHash)
			throws IOException, WebzException {
		return getInnerFileSystemImpl().getParentChildrenMetadataIfChanged(parentPathname, previousFolderHash);
	}

	@Override
	public Map<String, WebzMetadata> getChildPathnamesAndMetadata(String parentPathname) throws IOException, WebzException {
		return getInnerFileSystemImpl().getChildPathnamesAndMetadata(parentPathname);
	}

	@Override
	public Collection<String> getChildPathnames(String parentPathname) throws IOException, WebzException {
		return getInnerFileSystemImpl().getChildPathnames(parentPathname);
	}

	@Override
	public WebzFileDownloader getFileDownloader(String pathname) throws IOException, WebzException {
		return getInnerFileSystemImpl().getFileDownloader(pathname);
	}

	@Override
	public WebzMetadata createFolder(String pathname) throws IOException, WebzException {
		return getInnerFileSystemImpl().createFolder(pathname);
	}

	@Override
	public FileSpecific uploadFile(String pathname, InputStream content, long numBytes) throws IOException, WebzException {
		return getInnerFileSystemImpl().uploadFile(pathname, content);
	}

	@Override
	public FileSpecific uploadFile(String pathname, InputStream content) throws IOException, WebzException {
		return getInnerFileSystemImpl().uploadFile(pathname, content);
	}

	@Override
	public WebzMetadata move(String srcPathname, String destPathname) throws IOException, WebzException {
		return getInnerFileSystemImpl().move(srcPathname, destPathname);
	}

	@Override
	public WebzMetadata copy(String srcPathname, String destPathname) throws IOException, WebzException {
		return getInnerFileSystemImpl().copy(srcPathname, destPathname);
	}

	@Override
	public void delete(String pathname) throws IOException, WebzException {
		getInnerFileSystemImpl().delete(pathname);
	}

}