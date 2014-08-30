package org.terems.webz.cache;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

/** TODO !!! describe !!! **/
public class WebzByteArrayInputStream extends ByteArrayInputStream {

	/** TODO !!! describe !!! **/
	public long writeAvailableToOutputStream(OutputStream out) throws IOException {

		int available = available();

		out.write(this.buf, this.pos, available);
		return skip(available);
	}

	/**
	 * @see java.io.ByteArrayInputStream#ByteArrayInputStream(byte[])
	 */
	public WebzByteArrayInputStream(byte buf[]) {
		super(buf);
	}

	/**
	 * @see java.io.ByteArrayInputStream#ByteArrayInputStream(byte[], int, int)
	 */
	public WebzByteArrayInputStream(byte buf[], int offset, int length) {
		super(buf, offset, length);
	}

}
