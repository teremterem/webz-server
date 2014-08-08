package org.terems.webz.plugin;

import java.io.IOException;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFileFactory;

// TODO elaborate !!!
public abstract class BaseWebzFilter implements WebzFilter {

	private WebzFileFactory fileFactory;

	@Override
	public void init(WebzFileFactory fileFactory) throws IOException, WebzException {
		this.fileFactory = fileFactory;
	}

	@Override
	public WebzFileFactory getFileFactory() {
		return fileFactory;
	}

	@Override
	public void destroy() {
	}

}
