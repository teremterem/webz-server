package org.terems.webz;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** TODO !!! describe !!! **/
public interface WebzFilter extends WebzDestroyable {

	/** TODO !!! describe !!! **/
	public void init(WebzConfig appConfig) throws WebzException;

	/** TODO !!! describe !!! **/
	public void serve(HttpServletRequest req, HttpServletResponse resp, WebzChainContext chainContext) throws IOException, WebzException;

	public static final String HTTP_GET = "GET";
	public static final String HTTP_POST = "POST";
	public static final String HTTP_PUT = "PUT";
	public static final String HTTP_DELETE = "DELETE";
	public static final String HTTP_HEAD = "HEAD";
	public static final String HTTP_TRACE = "TRACE";
	public static final String HTTP_OPTIONS = "OPTIONS";

	public static final String HEADER_LAST_MODIFIED = "Last-Modified";
	public static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";
	public static final String HEADER_CONTENT_LENGTH = "Content-Length";

}
