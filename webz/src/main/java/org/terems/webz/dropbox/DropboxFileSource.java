package org.terems.webz.dropbox;

import java.io.IOException;
import java.io.OutputStream;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFileMetadata;
import org.terems.webz.base.BaseWebzFileSource;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;

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

	@Override
	public void getFile(String pathName, OutputStream out) throws IOException, WebzException {
		try {
			client.getFile(normalizePathName(dropboxPath + pathName), null, out);
		} catch (DbxException e) {
			throw new WebzException(e.getMessage(), e);
		}
	}

	@Override
	public WebzFileMetadata getMetadata(String pathName) throws WebzException {
		try {
			DbxEntry entry = client.getMetadata(normalizePathName(dropboxPath + pathName));
			if (entry == null) {
				return null;
			} else {
				return new DropboxFileMetadata(entry);
			}
		} catch (DbxException e) {
			throw new WebzException(e.getMessage(), e);
		}
	}

}
