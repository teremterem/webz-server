package org.terems.webz;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

public interface WebzFileSource {

	public WebzFileMetadata getFile(String pathName, OutputStream out) throws IOException, WebzException;

	public byte[] absorbFile(String pathName, int initialSize) throws IOException, WebzException;

	public WebzFileMetadata getMetadata(String pathName) throws WebzException;

	public Collection<WebzFileMetadata> getListOfChildren(String pathName) throws WebzException;

	public WebzFileMetadata createFolder(String pathName) throws WebzException;

	public WebzFileMetadata uploadFile(String pathName, String content, String encoding, boolean override) throws WebzException;

	public WebzFileMetadata move(String srcPathName, String destPathName) throws WebzException;

	public WebzFileMetadata copy(String srcPathName, String destPathName) throws WebzException;

}
