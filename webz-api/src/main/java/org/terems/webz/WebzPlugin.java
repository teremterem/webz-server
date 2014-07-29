package org.terems.webz;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO elaborate !!! (should it somewhat resemble regular Java Servlet ?)
// TODO is it a good idea to pass file factory as a parameter every time ?
public interface WebzPlugin {

	public void init(WebzFileFactory fileFactory) throws IOException, WebzException;

	public WebzFileFactory getFileFactory();

	public void service(HttpServletRequest req, HttpServletResponse resp) throws IOException, WebzException;

	public void destroy();

}
