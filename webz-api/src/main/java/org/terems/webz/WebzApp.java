package org.terems.webz;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** TODO !!! describe !!! **/
public interface WebzApp extends WebzDestroyable {

	/** TODO !!! describe !!! **/
	public void serve(HttpServletRequest req, HttpServletResponse resp) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public String getAppName();

}
