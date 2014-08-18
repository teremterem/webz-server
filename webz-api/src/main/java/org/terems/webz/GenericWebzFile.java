package org.terems.webz;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

// TODO move to WebZ Core together with File System and related classes ?
/** TODO !!! describe !!! **/
public class GenericWebzFile implements WebzFile {

	// TODO move this method to GenericWebzFileFactory ?
	@Deprecated
	public static String trimFileSeparators(String pathName) {
		pathName = pathName.trim();
		if (pathName.startsWith("/") || pathName.startsWith("\\")) {
			pathName = pathName.substring(1);
		}
		if (pathName.endsWith("/") || pathName.endsWith("\\")) {
			pathName = pathName.substring(0, pathName.length() - 1);
		}
		return pathName;
	}

	private String actualPathName;
	private WebzFileSystem fileSystem;

	/** TODO !!! describe !!! **/
	public GenericWebzFile(String actualPathName, WebzFileSystem fileSystem) {
		this.actualPathName = actualPathName;
		this.fileSystem = fileSystem;
	}

	/** TODO !!! describe !!! **/
	@Override
	public String getActualPathName() {
		return actualPathName;
	}

	private String getFileSystemMessageSuffix() {
		return "(file system: '" + fileSystem.getFileSystemUniqueId() + "')";
	}

	/** TODO document that this method will fetch metadata **/
	@Override
	public byte[] getFileContent() throws IOException, WebzException {

		WebzFileMetadata metadata = getMetadata();
		if (metadata == null) {
			throw new WebzException(actualPathName + " does not exist " + getFileSystemMessageSuffix());
		}

		WebzFileMetadata.FileSpecific fileSpecific = metadata.getFileSpecific();
		if (fileSpecific == null) {
			throw new WebzException(actualPathName + " is not a file " + getFileSystemMessageSuffix());
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream((int) fileSpecific.getNumberOfBytes());
		fileContentToOutputStream(out);
		return out.toByteArray();
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzFileMetadata getMetadata() throws IOException, WebzException {
		return fileSystem.getMetadata(actualPathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzFileMetadata fileContentToOutputStream(OutputStream out) throws IOException, WebzException {
		// unlike getFileContent() this method doesn't throw WebzException if path name does not exist or is not a file
		return fileSystem.fileContentToOutputStream(actualPathName, out);
	}

	/** TODO !!! describe !!! **/
	@Override
	public Collection<WebzFile> getChildren() throws IOException, WebzException {

		Collection<String> childPathNames = fileSystem.getChildPathNames(actualPathName);
		if (childPathNames == null) {
			return null;
		}

		Collection<WebzFile> children = new ArrayList<>(childPathNames.size());
		for (String childPathName : childPathNames) {
			children.add(new GenericWebzFile(childPathName, fileSystem));
		}
		return children;
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzFileMetadata createFolder() throws IOException, WebzException {
		return fileSystem.createFolder(actualPathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzFileMetadata uploadFile(byte[] content) throws IOException, WebzException {
		return fileSystem.uploadFile(actualPathName, content);
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzFileMetadata move(WebzFile destFile) throws IOException, WebzException {
		return move(destFile.getActualPathName());
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzFileMetadata copy(WebzFile destFile) throws IOException, WebzException {
		return copy(destFile.getActualPathName());
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzFileMetadata move(String destPathName) throws IOException, WebzException {
		return fileSystem.move(actualPathName, destPathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public WebzFileMetadata copy(String destPathName) throws IOException, WebzException {
		return fileSystem.copy(actualPathName, destPathName);
	}

	/** TODO !!! describe !!! **/
	@Override
	public void delete() throws IOException, WebzException {
		fileSystem.delete(actualPathName);
	}

}
