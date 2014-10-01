package org.terems.webz.internals;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.WebzDestroyable;
import org.terems.webz.WebzException;

/** TODO !!! describe !!! **/
public interface WebzServletContainerBridge extends WebzDestroyable {

	/** TODO !!! describe !!! **/
	public void serve(HttpServletRequest req, HttpServletResponse resp) throws IOException, WebzException;

}
