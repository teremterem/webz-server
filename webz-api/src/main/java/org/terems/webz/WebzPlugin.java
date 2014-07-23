package org.terems.webz;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO elaborate !!! (should it somewhat resemble regular Java Servlet ?)
// TODO is it a good idea to pass file factory as a parameter every time ?
public interface WebzPlugin {

	public void init(WebzFileFactory fileFactory) throws IOException, WebzException;

	public WebzFileFactory getFileFactory();

	public void fulfilRequest(HttpServletRequest req, HttpServletResponse resp);

}
