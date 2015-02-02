package org.terems.webz.base;

import org.terems.webz.WebzDestroyable;

/**
 * Basic implementation of {@code WebzDestroyable} which does nothing upon {@link #destroy()}.
 **/
public abstract class BaseWebzDestroyable implements WebzDestroyable {

	/** Do nothing by default... **/
	@Override
	public void destroy() {
	}

}
