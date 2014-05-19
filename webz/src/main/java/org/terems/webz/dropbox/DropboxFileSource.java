package org.terems.webz.dropbox;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFileMetadata;
import org.terems.webz.base.BaseWebzFileSource;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxEntry.WithChildren;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxWriteMode;

public class DropboxFileSource extends BaseWebzFileSource {

	private DbxClient client;
	private String dropboxPath;

	public DropboxFileSource(DbxClient client, String dropboxPath) {
		this.client = client;
		this.dropboxPath = dropboxPath;
	}

	private String normalizePathName(String pathName) {
		if (pathName.endsWith("/") || pathName.endsWith("\\")) {
			return pathName.substring(0, pathName.length() - 1);
		} else {
			return pathName;
		}
	}

	private WebzFileMetadata wrapMetadataSafely(DbxEntry entry) {
		return entry == null ? null : new DropboxFileMetadata(entry);
	}

	@Override
	public WebzFileMetadata getFile(String pathName, OutputStream out) throws IOException, WebzException {
		try {
			return wrapMetadataSafely(client.getFile(normalizePathName(dropboxPath + pathName), null, out));
		} catch (DbxException e) {
			throw new WebzException(e.getMessage(), e);
		}
	}

	@Override
	public Collection<WebzFileMetadata> getListOfChildren(String pathName) throws WebzException {
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
		// TODO ~~~ decide what to do with parent folder metadata ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ TODO \\
		// ~~~~~~~~ (not all APIs would return parent folder metadata together with children) ~~~~~~~~~~ \\
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\

		try {
			WithChildren dbxWithChildren = client.getMetadataWithChildren(normalizePathName(dropboxPath + pathName));
			if (dbxWithChildren == null) {
				throw new WebzException("'" + pathName + "' was not found");
			}
			if (dbxWithChildren.children == null) {
				throw new WebzException("'" + pathName + "' does not have children - probably it's not a folder");
			}

			List<WebzFileMetadata> webzChildren = new ArrayList<WebzFileMetadata>(dbxWithChildren.children.size());
			for (DbxEntry entry : dbxWithChildren.children) {
				webzChildren.add(wrapMetadataSafely(entry));
			}
			return webzChildren;
		} catch (DbxException e) {
			throw new WebzException(e.getMessage(), e);
		}
	}

	@Override
	public WebzFileMetadata getMetadata(String pathName) throws WebzException {
		try {
			return wrapMetadataSafely(client.getMetadata(normalizePathName(dropboxPath + pathName)));
		} catch (DbxException e) {
			throw new WebzException(e.getMessage(), e);
		}
	}

	@Override
	public WebzFileMetadata createFolder(String pathName) throws WebzException {
		String folderFullPathName = normalizePathName(dropboxPath + pathName);
		try {
			return wrapMetadataSafely(client.createFolder(folderFullPathName));
		} catch (DbxException e) {
			throw new WebzException(e.getMessage(), e);
		}
	}

	@Override
	public WebzFileMetadata uploadFile(String pathName, String content, String encoding, boolean override) throws WebzException {
		try {

			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
			// TODO ~ WHAT TO DO WITH DROPBOX NATIVE HISTORY? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ TODO \\
			// TODO ~ WHAT LOGIC TO APPLY IN CASE OF OVERRIDE=FALSE IN FINAL VERSION OF METHOD? ~~~~~~~ TODO \\
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\

			InputStream stream = new ByteArrayInputStream(content.getBytes(encoding));
			return wrapMetadataSafely(client.uploadFile(normalizePathName(dropboxPath + pathName),
					override ? DbxWriteMode.force() : DbxWriteMode.add(), stream.available(), stream));
		} catch (DbxException e) {
			throw new WebzException(e.getMessage(), e);
		} catch (IOException e) {
			throw new WebzException(e.getMessage(), e);
		}
	}

	@Override
	public WebzFileMetadata move(String srcPathName, String destPathName) throws WebzException {
		try {
			return wrapMetadataSafely(client.move(normalizePathName(dropboxPath + srcPathName), normalizePathName(dropboxPath
					+ destPathName)));
		} catch (DbxException e) {
			throw new WebzException(e.getMessage(), e);
		}
	}

	@Override
	public WebzFileMetadata copy(String srcPathName, String destPathName) throws WebzException {
		try {
			return wrapMetadataSafely(client.copy(normalizePathName(dropboxPath + srcPathName), normalizePathName(dropboxPath
					+ destPathName)));
		} catch (DbxException e) {
			throw new WebzException(e.getMessage(), e);
		}
	}

}
