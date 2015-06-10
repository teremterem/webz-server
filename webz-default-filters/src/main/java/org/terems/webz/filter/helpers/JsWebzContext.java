/*
 * WebZ Server can serve web pages from various local and remote file sources.
 * Copyright (C) 2014-2015  Oleksandr Tereschenko <http://www.terems.org/>
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

package org.terems.webz.filter.helpers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.terems.webz.WebzContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;

public class JsWebzContext extends JsWebzFiles {

	protected HttpServletRequest req;

	public JsWebzContext(WebzContext context, HttpServletRequest req) {
		super(context);
		this.req = req;
	}

	public WebzFile getCurrentFile() throws IOException, WebzException {
		return context.resolveFile(req);
	}

	public String resolveUri(WebzFile file) throws IOException, WebzException {
		return context.resolveUri(file, req);
	}

}
