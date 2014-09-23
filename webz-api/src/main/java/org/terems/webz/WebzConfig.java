package org.terems.webz;

import org.terems.webz.config.WebzConfigObject;

/** TODO !!! describe !!! **/
public interface WebzConfig {

	/** TODO !!! describe !!! **/
	public <T extends WebzConfigObject> T getAppConfigObject(Class<T> configObjectClass) throws WebzException;

}
