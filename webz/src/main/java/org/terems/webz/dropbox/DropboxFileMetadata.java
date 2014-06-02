package org.terems.webz.dropbox;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileSystem;
import org.terems.webz.base.BaseWebzFileMetadata;

import com.dropbox.core.DbxEntry;

public class DropboxFileMetadata extends BaseWebzFileMetadata<DbxEntry> {

	private static Logger LOG = LoggerFactory.getLogger(DropboxFileMetadata.class);

	protected DropboxFileMetadata(WebzFileSystem fileSystem, WebzFile file, DbxEntry dbxEntry) {
		super(fileSystem, file, dbxEntry);

		if (LOG.isTraceEnabled()) {
			LOG.trace("");
			LOG.trace("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			LOG.trace("!!!!! new DropboxFileMetadata(...); !!!!!");
			LOG.trace("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			LOG.trace(dbxEntry.toStringMultiline());
			LOG.trace("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			LOG.trace("");
		}
	}

	@Override
	public String getPathName() {
		return getMetadataObject().path;
	}

	@Override
	public String getName() {
		return getMetadataObject().name;
	}

	@Override
	public boolean isFile() {
		return getMetadataObject().isFile();
	}

	@Override
	public boolean isFolder() {
		return getMetadataObject().isFolder();
	}

	@Override
	protected FileSpecific initFileSpecific() {
		final DbxEntry.File dbxFile = getMetadataObject().asFile();

		if (dbxFile == null) {
			return null;
		} else {
			return new FileSpecific() {

				@Override
				public String getRevision() {
					return dbxFile.rev;
				}

				@Override
				public long getNumberOfBytes() {
					return dbxFile.numBytes;
				}

				@Override
				public Date getLastModified() {
					return dbxFile.lastModified;
				}
			};
		}
	}

}
