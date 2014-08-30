package org.terems.webz.cache;

import java.io.ByteArrayOutputStream;

/**
 * <code>WebzByteArrayOutputStream</code> is an extension of <code>ByteArrayOutputStream</code> class that lets us create
 * <code>WebzByteArrayInputStream</code> instances backed <b>directly</b> by it's internal byte-array buffer (see
 * {@link #createInputStream()} method) and avoid the necessity of copying the whole buffer content by calling the traditional
 * <code>toByteArray()</code> method.
 **/
public class WebzByteArrayOutputStream extends ByteArrayOutputStream {

	/**
	 * Creates an instance of <code>WebzByteArrayInputStream</code> backed by the internal byte-array buffer of the current
	 * <code>WebzByteArrayOutputStream</code> instance.
	 * <p>
	 * <b>NOTE:</b> WebzByteArrayOutputStream instance should not be written to after WebzByteArrayInputStream instance(s) is(are) created
	 * from it - otherwise the state of created WebzByteArrayInputStream instance(s) will be undefined...
	 * 
	 * @return an instance of newly created WebzByteArrayInputStream object.
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
