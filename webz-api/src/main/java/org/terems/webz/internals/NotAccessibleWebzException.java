package org.terems.webz.internals;

import org.terems.webz.WebzException;

/** TODO !!! describe !!! **/
@SuppressWarnings("serial")
public class NotAccessibleWebzException extends WebzException {

	public NotAccessibleWebzException() {
		super();
	}

	public NotAccessibleWebzException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public NotAccessibleWebzException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotAccessibleWebzException(String message) {
		super(message);
	}

	public NotAccessibleWebzException(Throwable cause) {
		super(cause);
	}

}
