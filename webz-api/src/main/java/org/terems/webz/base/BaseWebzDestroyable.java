package org.terems.webz.base;

import org.terems.webz.WebzDestroyable;

/**
 * Basic implementation of {@code BaseWebzDestroyable} to be extended by concrete implementations...
 **/
public abstract class BaseWebzDestroyable implements WebzDestroyable {

	/** Do nothing by default... **/
	@Override
	public void destroy() {
	}

}
