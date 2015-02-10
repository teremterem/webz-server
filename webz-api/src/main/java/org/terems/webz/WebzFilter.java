/*
 * WebZ Server is a server that can serve web pages from various sources.
 * Copyright (C) 2013-2015  Oleksandr Tereschenko <http://ww.webz.bz/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.terems.webz;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** TODO !!! describe !!! **/
public interface WebzFilter extends WebzDestroyable {

	/** TODO !!! describe !!! **/
	public void init(WebzConfig appConfig) throws WebzException;

	/** TODO !!! describe !!! **/
	public void serve(HttpServletRequest req, HttpServletResponse resp, WebzChainContext chainContext) throws IOException, WebzException;

	public static final String HTTP_GET = "GET";
	public static final String HTTP_POST = "POST";
	public static final String HTTP_PUT = "PUT";
	public static final String HTTP_DELETE = "DELETE";
	public static final String HTTP_HEAD = "HEAD";
	public static final String HTTP_TRACE = "TRACE";
	public static final String HTTP_OPTIONS = "OPTIONS";

	public static final String HEADER_LOCATION = "Location";

	public static final String HEADER_LAST_MODIFIED = "Last-Modified";
	public static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";
	public static final String HEADER_CONTENT_LENGTH = "Content-Length";
	public static final String HEADER_CONTENT_TYPE = "Content-Type";

}
