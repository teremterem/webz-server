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

package org.terems.webz.internals;

import java.util.Collection;
import java.util.Map;

import org.terems.webz.WebzDestroyable;
import org.terems.webz.WebzMetadata;
import org.terems.webz.internals.cache.ChildPathnamesHolder;
import org.terems.webz.internals.cache.FileContentHolder;

/** TODO !!! describe !!! **/
public interface WebzFileSystemCache extends WebzDestroyable {

	/** TODO !!! describe !!! **/
	public WebzFileSystemCache init(WebzFileSystemImpl fileSystemImpl, int filePayloadSizeThreshold);

	/** TODO !!! describe !!! **/
	public String getCacheTypeName();

	/** TODO !!! describe !!! **/
	public WebzMetadata fetchMetadata(String pathname);

	/** TODO !!! describe !!! **/
	public Map<String, WebzMetadata> fetchMetadata(Collection<String> pathnames);

	/** TODO !!! describe !!! **/
	public ChildPathnamesHolder fetchChildPathnamesHolder(String parentPathname);

	/** TODO !!! describe !!! **/
	public FileContentHolder fetchFileContentHolder(String pathname);

	/** TODO !!! describe !!! **/
	public void putMetadataIntoCache(String pathname, WebzMetadata metadata);

	/** TODO !!! describe !!! **/
	public void putChildPathnamesHolderIntoCache(String pathname, ChildPathnamesHolder childPathnamesHolder);

	/** TODO !!! describe !!! **/
	public void putFileContentHolderIntoCache(String pathname, FileContentHolder fileContentHolder);

	/** TODO !!! describe !!! **/
	public void dropMetadataFromCache(String pathname);

	/** TODO !!! describe !!! **/
	public void dropChildPathnamesHolderFromCache(String parentPathname);

	/** TODO !!! describe !!! **/
	public void dropFileContentHolderFromCache(String pathname);

}
