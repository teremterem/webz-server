package org.terems.webz.cache;

import java.io.ByteArrayOutputStream;

/**
 * {@code WebzByteArrayOutputStream} is an extension of {@code ByteArrayOutputStream} class that lets us create
 * {@code WebzByteArrayInputStream} instances backed <b>directly</b> by it's internal byte-array buffer (see {@link #createInputStream()}
 * method) and avoid the necessity of copying the whole buffer content by calling the traditional {@code toByteArray()} method.
 **/
public class WebzByteArrayOutputStream extends ByteArrayOutputStream {

	/**
	 * Creates an instance of {@code WebzByteArrayInputStream} backed by the internal byte-array buffer of the current
	 * {@code WebzByteArrayOutputStream} instance.
	 * <p>
	 * <b>NOTE:</b> {@code WebzByteArrayOutputStream} instance should not be written to after {@code WebzByteArrayInputStream} instance(s)
	 * is(are) created from it - otherwise the state of created {@code WebzByteArrayInputStream} instance(s) will be undefined...
	 * 
	 * @return newly created {@code WebzByteArrayInputStream} instance.
	 **/
	public WebzByteArrayInputStream createInputStream() {
		return new WebzByteArrayInputStream(this.buf, 0, this.count);
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
