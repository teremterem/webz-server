package org.terems.webz.plugin;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFileFactory;

// TODO elaborate !!! (should it somewhat resemble regular Java Servlet Filter ?)
public interface WebzFilter {

	public void init(WebzFileFactory fileFactory) throws IOException, WebzException;

	public WebzFileFactory getFileFactory();

	public void service(HttpServletRequest req, HttpServletResponse resp) throws IOException, WebzException;

	public void destroy();

}
