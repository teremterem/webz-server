package org.terems.webz;

import java.util.Properties;

public interface WebzPropertiesInitable extends WebzDestroyable {

	// TODO split WebzFileSystem into several interfaces, all inherited from WebzPropertiesInitable

	/** TODO !!! describe !!! **/
	public void init(Properties properties) throws WebzException;

	/** TODO !!! describe !!! **/
	public void init(Properties properties, boolean failOnError) throws WebzException;

	/** TODO !!! describe !!! **/
	public void init(WebzProperties webzProps) throws WebzException;

	/** TODO !!! describe !!! **/
	public void init(WebzProperties webzProps, boolean failOnError) throws WebzException;

}
