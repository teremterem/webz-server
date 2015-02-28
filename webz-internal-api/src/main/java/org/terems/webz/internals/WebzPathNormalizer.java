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

package org.terems.webz.internals;

import org.terems.webz.WebzPropertiesInitable;

public interface WebzPathNormalizer extends WebzPropertiesInitable {

	public String normalizePathname(String nonNormalizedPathname, boolean trimLeadingSlash);

	public boolean isNormalizedPathnameInvalid(String pathname);

	public String getParentPathname(String pathname);

	public String concatPathname(String basePath, String relativePathname);

	public boolean belongsToSubtree(String pathname, String subtreePath);

	public boolean isHidden(String pathname);

}
