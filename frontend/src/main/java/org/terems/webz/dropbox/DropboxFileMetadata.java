package org.terems.webz.dropbox;

import java.util.Date;

import org.terems.webz.WebzFileMetadata;

import com.dropbox.core.DbxEntry;

public class DropboxFileMetadata implements WebzFileMetadata {

	private DbxEntry dbxEntry;
	private boolean isFile;
	private DbxEntry.File asFile = null;

	public DropboxFileMetadata(DbxEntry dbxEntry) {
		this.dbxEntry = dbxEntry;
		isFile = dbxEntry.isFile();
		if (isFile) {
			asFile = dbxEntry.asFile();
		}
	}

	@Override
	public String getName() {
		return dbxEntry.name;
	}

	@Override
	public String getPath() {
		return dbxEntry.path;
	}

	@Override
	public boolean isFile() {
		return isFile;
	}

	@Override
	public Long getNumberOfBytes() {
		if (isFile) {
			return asFile.numBytes;
		} else {
			return null;
		}
	}

	@Override
	public Date getLastModified() {
		if (isFile) {
			return asFile.lastModified;
		} else {
			return null;
		}
	}

	@Override
	public String getRevision() {
		if (isFile) {
			return asFile.rev;
		} else {
			return null;
		}
	}

}
