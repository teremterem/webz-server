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

/** TODO !!! describe constants !!! **/
public class WebzDefaults {

	public static final boolean FS_CACHE_ENABLED = false;
	public static final boolean USE_METADATA_INFLATABLE_FILES = false;
	public static final String FS_IMPL_CLASS = "org.terems.webz.impl.LocalFileSystemImpl";
	public static final String FS_CACHE_IMPL_CLASS = "org.terems.webz.impl.cache.ehcache.EhcacheFileSystemCache";
	public static final int FS_CACHE_PAYLOAD_THRESHOLD_BYTES = 262144;

	public static final String MIMETYPE = "application/octet-stream";
	public static final String ENCODING = "UTF-8"; // ByteOrderMark.UTF_8.getCharsetName();

	public static final String WELCOME_EXTENSIONS_LIST = ".html";
	public static final String USE_PARENT_FOLDER_NAME = "..";
	public static final String WELCOME_FILENAMES_LIST = "index," + USE_PARENT_FOLDER_NAME;

}
