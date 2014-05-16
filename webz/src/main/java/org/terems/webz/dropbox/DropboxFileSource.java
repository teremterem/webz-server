package org.terems.webz.dropbox;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFileMetadata;
import org.terems.webz.base.BaseWebzFileSource;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
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

	@Override
	public void createFolder(String pathName) throws WebzException {
		String folderFullPathName = normalizePathName(dropboxPath + pathName);
		try {
			client.createFolder(folderFullPathName);
		} catch (DbxException e) {
			throw new WebzException(e.getMessage(), e);
		}
	}

	@Override
	public void uploadFile(String pathName, String content, String encoding, boolean override) throws WebzException {
		try {

			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
			// TODO ~ WHAT TO DO WITH DROPBOX NATIVE HISTORY? ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ TODO \\
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\

			InputStream stream = new ByteArrayInputStream(content.getBytes(encoding));
			client.uploadFile(normalizePathName(dropboxPath + pathName), override ? DbxWriteMode.force() : DbxWriteMode.add(),
					stream.available(), stream);
		} catch (DbxException e) {
			throw new WebzException(e.getMessage(), e);
		} catch (IOException e) {
			throw new WebzException(e.getMessage(), e);
		}
	}

	@Override
	public void moveFile(String srcPathName, String destPathName, boolean override) throws WebzException {
		try {

			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
			// TODO ~ IMPLEMENT MEANINGFUL OVERRIDE FLAG ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ TODO \\
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
			String quickfixedDest = normalizePathName(dropboxPath + destPathName);
			if (!override) {
				// quickfix!!!!!
				for (int number = 1;; number++) {
					String destToTry = quickfixedDest + number;

					DbxEntry entry = client.getMetadata(destToTry);
					if (entry == null) {
						// found available filename
						quickfixedDest = destToTry;
						break;
					}
				}
			}

			client.move(normalizePathName(dropboxPath + srcPathName), quickfixedDest);
		} catch (DbxException e) {
			throw new WebzException(e.getMessage(), e);
		}
	}

}
