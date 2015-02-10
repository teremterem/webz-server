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

/** TODO !!! describe !!! **/
public interface WebzMetadata {

	/** TODO !!! describe !!! **/
	public String getName() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public boolean isFile() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public boolean isFolder() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public FileSpecific getFileSpecific() throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	public interface FileSpecific extends WebzMetadata {

		/** TODO !!! describe !!! **/
		public long getNumberOfBytes() throws IOException, WebzException;

		/** TODO !!! describe !!! **/
		public Long getLastModified() throws IOException, WebzException;

		/** TODO !!! describe !!! **/
		public String getRevision() throws IOException, WebzException;

	}

}
