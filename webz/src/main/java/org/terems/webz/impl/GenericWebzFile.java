package org.terems.webz.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileFactory;
import org.terems.webz.WebzMetadata;
import org.terems.webz.WebzFileSystem;

/** TODO !!! describe !!! **/
public class GenericWebzFile implements WebzFile {

	private String pathName;
	private WebzFileFactory fileFactory;
	private WebzFileSystem fileSystem;

	/** TODO !!! describe !!! **/
	public GenericWebzFile(String pathName, WebzFileFactory fileFactory, WebzFileSystem fileSystem) {
		this.pathName = pathName;
		this.fileFactory = fileFactory;
		this.fileSystem = fileSystem;
	}

	/** TODO !!! describe !!! **/
	@Override
	public String getPathName() {
		return pathName;
	}

	private String getFileSystemMessageSuffix() {
		return "(file system: '" + fileSystem.getFileSystemUniqueId() + "')";
	}

	/** TODO document that this method will fetch metadata **/
	@Override
	public byte[] getFileContent() throws IOException, WebzException {

		WebzMetadata metadata = getMetadata();
		if (metadata == null) {
			throw new WebzException(pathName + " does not exist " + getFileSystemMessageSuffix());
		}

		WebzMetadata.FileSpecific fileSpecific = metadata.getFileSpecific();
		if (fileSpecific == null) {
			throw new WebzException(pathName + " is not a file " + getFileSystemMessageSuffix());
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream((int) fileSpecific.getNumberOfBytes());
		fileContentToOutputStream(out);
		return out.toByteArray();
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzMetadata getMetadata() throws IOException, WebzException {
		return fileSystem.getMetadata(pathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzMetadata fileContentToOutputStream(OutputStream out) throws IOException, WebzException {
		// unlike getFileContent() this method doesn't throw WebzException if path name does not exist or is not a file
		return fileSystem.fileContentToOutputStream(pathName, out);
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzFile getParent() throws IOException, WebzException {

		String parentPathName = fileSystem.getParentPathName(pathName);
		if (parentPathName == null) {
			return null;
		}

		return fileFactory.get(parentPathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public Collection<WebzFile> getChildren() throws IOException, WebzException {

		Collection<String> childPathNames = fileSystem.getChildPathNames(pathName);
		if (childPathNames == null) {
			return null;
		}

		Collection<WebzFile> children = new ArrayList<>(childPathNames.size());
		for (String childPathName : childPathNames) {
			children.add(fileFactory.get(childPathName));
		}
		return children;
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzMetadata createFolder() throws IOException, WebzException {
		return fileSystem.createFolder(pathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzMetadata uploadFile(byte[] content) throws IOException, WebzException {
		return fileSystem.uploadFile(pathName, content);
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzMetadata move(WebzFile destFile) throws IOException, WebzException {
		return move(destFile.getPathName());
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzMetadata copy(WebzFile destFile) throws IOException, WebzException {
		return copy(destFile.getPathName());
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzMetadata move(String destPathName) throws IOException, WebzException {
		return fileSystem.move(pathName, destPathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzMetadata copy(String destPathName) throws IOException, WebzException {
		return fileSystem.copy(pathName, destPathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public void delete() throws IOException, WebzException {
		fileSystem.delete(pathName);
	}

}
