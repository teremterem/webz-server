package org.terems.webz;

/** TODO !!! describe !!! **/
public interface WebzDestroyable {

	/**
	 * <b>ATTENTION:</b> each {@code WebzDestroyable} object should only care about releasing it's internal resources and not try to
	 * explicitly destroy any other {@code WebzDestroyable}'s (even if they are embedded in this object) as this job is supposed to be done
	 * by corresponding {@code WebzObjectFactory}.
	 **/
	public void destroy();

}
