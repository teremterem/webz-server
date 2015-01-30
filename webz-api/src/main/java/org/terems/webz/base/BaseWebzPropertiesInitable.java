package org.terems.webz.base;

import java.util.Properties;

import org.terems.webz.WebzException;
import org.terems.webz.WebzProperties;
import org.terems.webz.WebzPropertiesInitable;

/** TODO !!! describe !!! **/
public abstract class BaseWebzPropertiesInitable extends BaseWebzDestroyable implements WebzPropertiesInitable {

	private WebzProperties properties;

	/** Do nothing by default... **/
	protected void init() throws WebzException {
	}

	/** TODO !!! describe !!! **/
	protected WebzProperties getProperties() {
		return properties;
	}

	/** TODO !!! describe !!! **/
	@Override
	public final void init(Properties properties) throws WebzException {

		if (properties == null) {
			throw new NullPointerException("null Properties");
		}
		init(new WebzProperties(properties));
	}

	/** TODO !!! describe !!! **/
	@Override
	public final void init(WebzProperties webzProperties) throws WebzException {

		if (webzProperties == null) {
			throw new NullPointerException("null WebzProperties");
		}
		this.properties = webzProperties;
		init();
	}

}
