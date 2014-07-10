package org.terems.webz.impl.dropbox;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFileMetadata;
import org.terems.webz.impl.BaseWebzFileSystem;
import org.terems.webz.impl.GenericWebzFile;
import org.terems.webz.internal.FreshParentChildrenMetadata;
import org.terems.webz.internal.ParentChildrenMetadata;
import org.terems.webz.internal.WebzFileDownloader;

import com.dropbox.core.DbxAccountInfo;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxClient.Downloader;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxWriteMode;
import com.dropbox.core.util.IOUtil;
import com.dropbox.core.util.Maybe;

public class DropboxFileSystem extends BaseWebzFileSystem {

	private static Logger LOG = LoggerFactory.getLogger(DropboxFileSystem.class);

	private DbxClient dbxClient;

	private final String dbxBasePath;
	private final String fileSystemUniqueId;

	public DropboxFileSystem(DbxClient dbxClient, String dbxBasePath) throws WebzException {
		try {
			this.dbxClient = dbxClient;
			this.dbxBasePath = "/" + GenericWebzFile.trimFileSeparators(dbxBasePath);

			DbxAccountInfo dbxAccountInfo = dbxClient.getAccountInfo();
			// TODO what is referralLink? can it identify Dropbox file system uniquely?
			this.fileSystemUniqueId = "Dropbox-" + dbxAccountInfo.referralLink + "-" + this.dbxBasePath;

			LOG.info("'" + this.fileSystemUniqueId + "' file system was created for the following Dropbox account: "
					+ dbxAccountInfo);
		} catch (DbxException e) {
			throw new WebzException(e);
		}
	}

	/**
	 * @param pathName
	 *            is supposed to contain neither leading/trailing white spaces nor leading/trailing path separators, otherwise
	 *            this method will not work properly... (this condition is ensured by GenericWebzFile implementation)
	 * @return pathName prefixed with dropbox base path component
	 */
	protected String dropboxPathName(String pathName) throws IOException, WebzException {
		if (pathName == null) {
			throw new NullPointerException("null pathName cannot be transformed into dropboxPathName");
		}
		if ("".equals(pathName)) {
			pathName = dbxBasePath;
		} else {
			pathName = dbxBasePath + "/" + pathName;
		}

		return pathName;
	}

	private WebzFileMetadata wrapMetadataSafely(DbxEntry dbxEntry) {
		return dbxEntry == null ? null : ((WebzFileMetadata) new DropboxFileMetadata(dbxEntry));
	}

	@Override
	public String getFileSystemUniqueId() {
		return this.fileSystemUniqueId;
	}

	@Override
	public WebzFileMetadata getMetadata(String pathName) throws IOException, WebzException {
		try {
			return wrapMetadataSafely(dbxClient.getMetadata(dropboxPathName(pathName)));
		} catch (DbxException e) {
			throw new WebzException(e);
		}
	}

	@Override
	public ParentChildrenMetadata getParentChildrenMetadata(String parentPathName) throws IOException, WebzException {
		try {
			return populateParentChildrenMetadata(parentPathName,
					dbxClient.getMetadataWithChildren(dropboxPathName(parentPathName)));
		} catch (DbxException e) {
			throw new WebzException(e);
		}
	}

	@Override
	public FreshParentChildrenMetadata getParentChildrenMetadataIfChanged(String parentPathName, Object previousFolderHash)
			throws IOException, WebzException {

		if (previousFolderHash == null) {
			return new FreshParentChildrenMetadata(getParentChildrenMetadata(parentPathName));
		}

		try {
			Maybe<DbxEntry.WithChildren> maybe = dbxClient.getMetadataWithChildrenIfChanged(dropboxPathName(parentPathName),
					assertFolderHash(previousFolderHash));

			return maybe.isNothing() ? null : new FreshParentChildrenMetadata(populateParentChildrenMetadata(parentPathName,
					maybe.getJust()));

		} catch (DbxException e) {
			throw new WebzException(e);
		}
	}

	private String assertFolderHash(Object folderHash) {
		if (!(folderHash instanceof String)) {
			throw new ClassCastException("dropbox folder hash should be of type String");
		}
		return (String) folderHash;
	}

	private ParentChildrenMetadata populateParentChildrenMetadata(String parentPathName, DbxEntry.WithChildren withChildren) {

		if (withChildren == null) {
			return null;
		}

		ParentChildrenMetadata parentChildrenMetadata = new ParentChildrenMetadata();
		parentChildrenMetadata.parentMetadata = wrapMetadataSafely(withChildren.entry);
		parentChildrenMetadata.folderHash = withChildren.hash;

		if (withChildren.children != null) {
			parentChildrenMetadata.childPathNamesAndMetadata = new LinkedHashMap<>(withChildren.children.size());

			for (DbxEntry dbxChild : withChildren.children) {
				parentChildrenMetadata.childPathNamesAndMetadata.put(parentPathName + "/" + dbxChild.name,
						wrapMetadataSafely(dbxChild));
			}
		}

		return parentChildrenMetadata;
	}

	@Override
	public WebzFileMetadata fileContentToOutputStream(String pathName, OutputStream out) throws IOException, WebzException {
		try {
			return wrapMetadataSafely(dbxClient.getFile(dropboxPathName(pathName), null, out));
		} catch (DbxException e) {
			throw new WebzException(e);
		}
	}

	@Override
	public WebzFileDownloader getFileContentDownloader(String pathName) throws IOException, WebzException {
		try {
			final Downloader dbxDownloader = dbxClient.startGetFile(dropboxPathName(pathName), null);
			if (dbxDownloader == null) {
				return null;
			}

			return new WebzFileDownloader(wrapMetadataSafely(dbxDownloader.metadata)) {

				@Override
				public void fileContentToOutputStream(OutputStream out) throws IOException, WebzException {
					try {
						IOUtil.copyStreamToStream(dbxDownloader.body, out);
					} finally {
						dbxDownloader.close();
					}
				}
			};
		} catch (DbxException e) {
			throw new WebzException(e);
		}
	}

	@Override
	public WebzFileMetadata createFolder(String pathName) throws IOException, WebzException {
		try {
			return wrapMetadataSafely(dbxClient.createFolder(dropboxPathName(pathName)));
		} catch (DbxException e) {
			throw new WebzException(e);
		}
	}

	@Override
	public WebzFileMetadata uploadFile(String pathName, byte[] content) throws IOException, WebzException {
		try {
			InputStream stream = new ByteArrayInputStream(content);
			return wrapMetadataSafely(dbxClient.uploadFile(dropboxPathName(pathName), DbxWriteMode.force(), stream.available(),
					stream));
		} catch (DbxException e) {
			throw new WebzException(e);
		}
	}

	@Override
	public WebzFileMetadata move(String srcPathName, String destPathName) throws IOException, WebzException {
		try {
			return wrapMetadataSafely(dbxClient.move(dropboxPathName(srcPathName), dropboxPathName(destPathName)));
		} catch (DbxException e) {
			throw new WebzException(e);
		}
	}

	@Override
	public WebzFileMetadata copy(String srcPathName, String destPathName) throws IOException, WebzException {
		try {
			return wrapMetadataSafely(dbxClient.copy(dropboxPathName(srcPathName), dropboxPathName(destPathName)));
		} catch (DbxException e) {
			throw new WebzException(e);
		}
	}

	@Override
	public void delete(String pathName) throws IOException, WebzException {
		try {
			dbxClient.delete(dropboxPathName(pathName));
		} catch (DbxException e) {
			throw new WebzException(e);
		}
	}

}
