package org.terems.webz.obsolete;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

// TODO delete this class completely
@Deprecated
public class ObsoleteCacheTappingOutputStream extends OutputStream {

	private OutputStream out;
	private ByteArrayOutputStream cacheOut;
	private int filePayloadSizeThreshold;

	public ObsoleteCacheTappingOutputStream(OutputStream out, int initialCachedSize, int filePayloadSizeThreshold) {
		this.out = out;
		cacheOut = new ByteArrayOutputStream(initialCachedSize);
		this.filePayloadSizeThreshold = filePayloadSizeThreshold;
	}

	private boolean assertFutureCacheOutSize(int bytesAboutToBeAdded) {
		if (cacheOut == null) {
			// cacheOut was already dropped
			return false;
		} else {
			boolean futureSizeTooBig = cacheOut.size() + bytesAboutToBeAdded > filePayloadSizeThreshold;
			if (futureSizeTooBig) {
				// drop cacheOut if it is going to be too big
				cacheOut = null;
			}
			return !futureSizeTooBig;
		}
	}

	@Override
	public void write(int b) throws IOException {

		out.write(b);
		if (assertFutureCacheOutSize(1)) {
			cacheOut.write(b);
		}
	}

	@Override
	public void write(byte[] b) throws IOException {

		out.write(b);
		if (assertFutureCacheOutSize(b.length)) {
			cacheOut.write(b);
		}
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {

		out.write(b, off, len);
		if (assertFutureCacheOutSize(len)) {
			cacheOut.write(b, off, len);
		}
	}

	@Override
	public void flush() throws IOException {
		out.flush();
	}

	@Override
	public void close() throws IOException {
		out.close();
	}

	public byte[] getPayloadToBeCached() {
		return cacheOut == null ? null : cacheOut.toByteArray();
	}

}
