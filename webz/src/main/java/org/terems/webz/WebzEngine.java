package org.terems.webz;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface WebzEngine {

	public void fulfilRequest(HttpServletRequest req, HttpServletResponse resp);

}
