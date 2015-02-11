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

package org.terems.webz.internals.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * {@code WebzByteArrayOutputStream} is an extension of {@code java.io.ByteArrayOutputStream} class that lets us create
 * {@code ByteArrayInputStream} instances backed by it's internal byte-array buffer <b>directly</b> (see {@link #createInputStream()}) and
 * avoid copying of the whole buffer content, which usually happens when traditional {@link #toByteArray()} is called.
 **/
public class WebzByteArrayOutputStream extends ByteArrayOutputStream {

	/**
	 * Creates an instance of {@code java.io.ByteArrayInputStream} backed by the internal byte-array buffer of the current
	 * {@code WebzByteArrayOutputStream} instance.
	 * <p>
	 * <b>NOTE:</b> {@code WebzByteArrayOutputStream} instance should not be written to after {@code ByteArrayInputStream} instance(s)
	 * is(are) created from it - otherwise the state of created {@code ByteArrayInputStream} instance(s) will be undefined...
	 * 
	 * @return newly created {@code ByteArrayInputStream} instance.
	 **/
	public ByteArrayInputStream createInputStream() {
		return new ByteArrayInputStream(this.buf, 0, this.count);
	}

	/**
	 * @see java.io.ByteArrayOutputStream#ByteArrayOutputStream()
	 */
	public WebzByteArrayOutputStream() {
		super();
	}

	/**
	 * @see java.io.ByteArrayOutputStream#ByteArrayOutputStream(int)
	 */
	public WebzByteArrayOutputStream(int size) {
		super(size);
	}

}
