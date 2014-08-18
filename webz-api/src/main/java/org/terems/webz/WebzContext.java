package org.terems.webz;

/** TODO !!! describe !!! **/
public interface WebzContext extends WebzFileFactoryKeeper {

	/** TODO !!! describe !!! **/
	public WebzFile getRequestedFile();

	/** TODO !!! describe !!! **/
	public WebzResource webzGet(String uriORurl);

}
