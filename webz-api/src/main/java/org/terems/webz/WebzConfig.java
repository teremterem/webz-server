package org.terems.webz;

import java.io.IOException;

/** TODO !!! describe !!! **/
public interface WebzConfig {

	/** TODO !!! describe !!! **/
	public WebzFile resolveConfigFolder() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public WebzFile getFile(String pathInfo) throws IOException, WebzException;

}
