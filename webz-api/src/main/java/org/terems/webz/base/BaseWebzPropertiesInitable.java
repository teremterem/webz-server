package org.terems.webz.base;

import java.util.Properties;

import org.terems.webz.WebzException;
import org.terems.webz.WebzProperties;
import org.terems.webz.WebzPropertiesInitable;
import org.terems.webz.util.WebzUtils;

/** TODO !!! describe !!! **/
public abstract class BaseWebzPropertiesInitable extends BaseWebzDestroyable implements WebzPropertiesInitable {

	private WebzProperties webzProperties;

	/** Do nothing by default... **/
	protected void init() throws WebzException {
	}

	/** TODO !!! describe !!! **/
	protected WebzProperties getWebzProperties() {
		return webzProperties;
	}

	/** TODO !!! describe !!! **/
	@Override
	public final void init(Properties properties) throws WebzException {
		init(properties, false);
	}

	/** TODO !!! describe !!! **/
	@Override
	public final void init(Properties properties, boolean failIfNotFound) throws WebzException {

		if (properties == null && failIfNotFound) {
			throw WebzUtils.newWebzNotFound(Properties.class);
		}
		init(new WebzProperties(properties), failIfNotFound);
	}

	/** TODO !!! describe !!! **/
	@Override
	public final void init(WebzProperties webzProps) throws WebzException {
		init(webzProps, false);
	}

	/** TODO !!! describe !!! **/
	@Override
	public final void init(WebzProperties webzProps, boolean failIfNotFound) throws WebzException {

		if (webzProps == null && failIfNotFound) {
			throw WebzUtils.newWebzNotFound(WebzProperties.class);
		}
		this.webzProperties = webzProps;
		init();
	}

}
