package org.terems.webz.config;

import java.io.IOException;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.base.BaseWebzDestroyable;

/** TODO !!! describe !!! **/
public abstract class WebzConfigObject extends BaseWebzDestroyable {

	/** TODO !!! describe !!! **/
	public abstract void init(WebzFile configFolder) throws IOException, WebzException;

	private volatile boolean initialized;
	private final Object mutex = new Object();

	/** TODO !!! describe !!! For internal use only... **/
	public final boolean doOneTimeInit(WebzFile configFolder) throws WebzException {

		if (!initialized) {
			synchronized (mutex) {
				if (!initialized) {

					try {
						init(configFolder);
						initialized = true;
						return true;

					} catch (IOException e) {
						throw new WebzException(e);
					}
				}
			}
		}
		return false;
	}

}
