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

/** TODO !!! describe !!! **/
public interface WebzDestroyable {

	/**
	 * <b>ATTENTION:</b> each {@code WebzDestroyable} object should only care about releasing it's internal resources and not try to
	 * explicitly destroy any other {@code WebzDestroyable}'s (even if they are embedded in this object) as this job is supposed to be done
	 * by corresponding {@code WebzObjectFactory}.
	 **/
	public void destroy();

}
