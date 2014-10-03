package org.terems.webz;

import java.util.Properties;

/** TODO !!! describe !!! **/
public interface WebzPropertiesInitable {

	/** TODO !!! describe !!! **/
	public void init(Properties properties) throws WebzException;

	/** TODO !!! describe !!! **/
	public void init(Properties properties, boolean failOnError) throws WebzException;

	/** TODO !!! describe !!! **/
	public void init(WebzProperties webzProperties) throws WebzException;

	/** TODO !!! describe !!! **/
	public void init(WebzProperties webzProperties, boolean failOnError) throws WebzException;

}
