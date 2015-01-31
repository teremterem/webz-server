package org.terems.webz.internals.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * {@code WebzByteArrayOutputStream} is an extension of {@code ByteArrayOutputStream} class that lets us create {@code ByteArrayInputStream}
 * instances backed by it's internal byte-array buffer <b>directly</b> (see {@link #createInputStream()}) and avoid copying of the whole
 * buffer content which happens when traditional {@code toByteArray()} is called.
 **/
public class WebzByteArrayOutputStream extends ByteArrayOutputStream {

	/**
	 * Creates an instance of {@code ByteArrayInputStream} backed by the internal byte-array buffer of the current
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
