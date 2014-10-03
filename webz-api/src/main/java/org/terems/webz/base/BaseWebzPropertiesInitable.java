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
		init(properties, false);
	}

	/** TODO !!! describe !!! **/
	@Override
	public final void init(Properties properties, boolean failIfNotFound) throws WebzException {
		init(properties == null ? null : new WebzProperties(properties), failIfNotFound);
	}

	/** TODO !!! describe !!! **/
	@Override
	public final void init(WebzProperties webzProperties) throws WebzException {
		init(webzProperties, false);
	}

	/** TODO !!! describe !!! **/
	@Override
	public final void init(WebzProperties webzProperties, boolean failIfNotFound) throws WebzException {

		if (webzProperties == null) {
			if (failIfNotFound) {

				throw new WebzException("properties not found");
			} else {
				this.properties = new WebzProperties();
			}
		} else {
			this.properties = webzProperties;
		}
		init();
	}

}
