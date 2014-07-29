package org.terems.webz;

import java.io.IOException;

// TODO elaborate !!!
public abstract class BaseWebzPlugin implements WebzPlugin {

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
