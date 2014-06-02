package org.terems.webz.dropbox;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileMetadata;
import org.terems.webz.WebzFileSystem;
import org.terems.webz.base.BaseWebzFile;
import org.terems.webz.base.BaseWebzFileMetadata;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxWriteMode;

public class DropboxFileSystem implements WebzFileSystem {

	private DbxClient dbxClient;
	private String dbxBasePath;

	public DropboxFileSystem(DbxClient dbxClient, String dbxBasePath) {
		this.dbxClient = dbxClient;
		this.dbxBasePath = dbxBasePath.trim();
	}

	protected String dropboxPathName(WebzFile file) throws IOException, WebzException {
		return dbxBasePath + file.getPathName();
	}

	@SuppressWarnings("unchecked")
	private WebzFileMetadata<Object> wrapMetadataSafely(WebzFile file, DbxEntry dbxEntry) {
		return dbxEntry == null ? null : ((WebzFileMetadata<Object>) ((BaseWebzFileMetadata<?>) new DropboxFileMetadata(this,
				file, dbxEntry)));
	}

	private void setMetadataIfNotNull(WebzFile file, DbxEntry dbxEntry) {
		WebzFileMetadata<Object> metadata = wrapMetadataSafely(file, dbxEntry);
		if (metadata != null) {
			file.setMetadataThreadSafe(metadata);
		}
	}

	@Override
	public WebzFile get(String pathName) {
		return new BaseWebzFile(this, pathName);
	}

	@Override
	public WebzFile fetchMetadata(WebzFile file) throws IOException, WebzException {
		try {
			file.setMetadataThreadSafe(wrapMetadataSafely(file, dbxClient.getMetadata(dropboxPathName(file))));
			return file;
		} catch (DbxException e) {
			throw new WebzException(e);
		}
	}

	@Override
	public WebzFileMetadata.FolderSpecific fetchMetadataWithChildren(WebzFile file, WebzFileMetadata<Object> metadata)
			throws IOException, WebzException {
		try {
			DbxEntry.WithChildren wc = dbxClient.getMetadataWithChildren(dropboxPathName(file));
			if (wc == null) {
				return null;
			}
			metadata.setMetadataObjectThreadSafe(wc.entry);

			List<WebzFile> webzChildren;
			if (wc.children == null) {
				webzChildren = Collections.emptyList();
			} else {
				webzChildren = new ArrayList<>(wc.children.size());
				for (DbxEntry dbxChild : wc.children) {
					WebzFile webzChild = new BaseWebzFile(this, file.getPathName() + "/" + dbxChild.name);
					webzChild.setMetadataThreadSafe(wrapMetadataSafely(file, dbxChild));
					webzChildren.add(webzChild);
				}
			}

			return BaseWebzFileMetadata.newFolderSpecific(wc.hash, webzChildren);

		} catch (DbxException e) {
			throw new WebzException(e);
		}
	}

	@Override
	public WebzFile fileContentToOutputStream(WebzFile file, OutputStream out) throws IOException, WebzException {
		try {
			setMetadataIfNotNull(file, dbxClient.getFile(dropboxPathName(file), null, out));
			return file;
		} catch (DbxException e) {
			throw new WebzException(e);
		}
	}

	@Override
	public WebzFile createFolder(WebzFile file) throws IOException, WebzException {
		try {
			setMetadataIfNotNull(file, dbxClient.createFolder(dropboxPathName(file)));
			return file;
		} catch (DbxException e) {
			throw new WebzException(e);
		}
	}

	@Override
	public WebzFile uploadFile(WebzFile file, byte[] content, boolean override) throws IOException, WebzException {
		try {

			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
			// TODO ~ WHAT TO DO WITH DROPBOX NATIVE HISTORY? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ TODO \\
			// TODO ~ WHAT LOGIC TO APPLY IN CASE OF OVERRIDE=FALSE IN FINAL VERSION OF METHOD? ~~~~~~~ TODO \\
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\

			InputStream stream = new ByteArrayInputStream(content);
			setMetadataIfNotNull(
					file,
					dbxClient.uploadFile(dropboxPathName(file), override ? DbxWriteMode.force() : DbxWriteMode.add(),
							stream.available(), stream));
			return file;
		} catch (DbxException e) {
			throw new WebzException(e);
		}
	}

	@Override
	public WebzFile move(WebzFile srcFile, WebzFile destFile) throws IOException, WebzException {
		try {
			setMetadataIfNotNull(destFile, dbxClient.move(dropboxPathName(srcFile), dropboxPathName(destFile)));

			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
			// ~~~~~ TODO check if exactly destFile should be returned ~~~~~ \\
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
			return destFile;
		} catch (DbxException e) {
			throw new WebzException(e);
		}
	}

	@Override
	public WebzFile copy(WebzFile srcFile, WebzFile destFile) throws IOException, WebzException {
		try {
			setMetadataIfNotNull(destFile, dbxClient.copy(dropboxPathName(srcFile), dropboxPathName(destFile)));

			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
			// ~~~~~ TODO check if exactly destFile should be returned ~~~~~ \\
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
			return destFile;
		} catch (DbxException e) {
			throw new WebzException(e);
		}
	}

}
