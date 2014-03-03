package org.terems.webz;

import java.io.IOException;
import java.io.OutputStream;

public interface WebzFileSource {

	public void getFile(String pathName, OutputStream out) throws IOException, WebzException;

	public byte[] absorbFile(String pathName, int initialSize) throws IOException, WebzException;

	public WebzFileMetadata getMetadata(String pathName) throws WebzException;

}
