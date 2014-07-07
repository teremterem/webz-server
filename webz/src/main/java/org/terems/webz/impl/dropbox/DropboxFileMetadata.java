package org.terems.webz.impl.dropbox;

import java.io.IOException;
import java.util.Date;

import org.terems.webz.WebzException;
import org.terems.webz.impl.base.BaseWebzFileMetadata;

import com.dropbox.core.DbxEntry;

public class DropboxFileMetadata extends BaseWebzFileMetadata {

	private DbxEntry dbxEntry;
	private DbxEntry.File dbxFile;

	public DropboxFileMetadata(DbxEntry dbxEntry) {
		this.dbxEntry = dbxEntry;
		dbxFile = dbxEntry.isFile() ? dbxEntry.asFile() : null;
	}

	@Override
	public String getName() throws IOException, WebzException {
		return dbxEntry.name;
	}

	@Override
	public boolean isFile() throws IOException, WebzException {
		return dbxEntry.isFile();
	}

	@Override
	public boolean isFolder() throws IOException, WebzException {
		return dbxEntry.isFolder();
	}

	@Override
	public long getNumberOfBytes() {
		return dbxFile.numBytes;
	}

	@Override
	public Date getLastModified() {
		return dbxFile.lastModified;
	}

	@Override
	public String getRevision() {
		return dbxFile.rev;
	}

	@Override
	public String _getNativePathName() throws IOException, WebzException {
		return dbxEntry.path;
	}

}
