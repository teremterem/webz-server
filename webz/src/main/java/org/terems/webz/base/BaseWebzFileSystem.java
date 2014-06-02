package org.terems.webz.base;

import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileSystem;

public abstract class BaseWebzFileSystem implements WebzFileSystem {

	@Override
	public WebzFile get(String pathName) {
		return new BaseWebzFile(this, pathName);
	}

}
