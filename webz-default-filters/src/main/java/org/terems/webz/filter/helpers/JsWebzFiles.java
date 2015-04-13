package org.terems.webz.filter.helpers;

import java.io.IOException;

import org.terems.webz.WebzContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;

public class JsWebzFiles {

	protected WebzContext context;

	public JsWebzFiles(WebzContext context) {
		this.context = context;
	}

	public WebzFile getFile(String pathInfo) throws IOException, WebzException {
		return context.getFile(pathInfo);
	}

}
