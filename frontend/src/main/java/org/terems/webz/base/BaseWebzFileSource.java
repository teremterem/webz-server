package org.terems.webz.base;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFileSource;

public abstract class BaseWebzFileSource implements WebzFileSource {

	@Override
	public byte[] absorbFile(String pathName, int initialSize) throws IOException, WebzException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(initialSize);
		getFile(pathName, out);
		return out.toByteArray();
	}

}
