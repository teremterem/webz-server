/*
 * WebZ Server is a server that can serve web pages from various sources.
 * Copyright (C) 2013-2015  Oleksandr Tereschenko <http://www.terems.org/>
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
