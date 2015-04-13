package org.terems.webz.filter.helpers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.terems.webz.WebzContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;

public class JsWebzContext extends JsWebzFiles {

	protected HttpServletRequest req;

	public JsWebzContext(WebzContext context, HttpServletRequest req) {
		super(context);
		this.req = req;
	}

	public WebzFile getCurrentFile() throws IOException, WebzException {
		return context.resolveFile(req);
	}

	public String resolveUri(WebzFile file) throws IOException, WebzException {
		return context.resolveUri(file, req);
	}

}
