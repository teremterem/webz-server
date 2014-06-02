package org.terems.webz.base;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.terems.webz.WebzConstants;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileSystem;

public class BaseFileSystemCache extends WebzFileSystemProxy {

	private WebzFileSystem fileSystem;
	private int filePayloadSizeThreshold;

	private class CachedFilePayload {
		private String revision;
		private byte[] payload;
	}

	private Map<String, SoftReference<CachedFilePayload>> cachedPayloads = new ConcurrentHashMap<>();

	public BaseFileSystemCache(WebzFileSystem fileSystem, int filePayloadSizeThreshold) {
		this.fileSystem = fileSystem;
		this.filePayloadSizeThreshold = filePayloadSizeThreshold;
	}

	public BaseFileSystemCache(WebzFileSystem fileSystem) {
		this(fileSystem, WebzConstants.DEFAULT_FILE_PAYLOAD_SIZE_THRESHOLD_TO_CACHE);
	}

	@Override
	protected WebzFileSystem getFileSystem() {
		return fileSystem;
	}

	private class CacheTappingOutputStream extends OutputStream {

		private OutputStream out;
		private ByteArrayOutputStream cacheOut;

		public CacheTappingOutputStream(OutputStream out, int initialCachedSize) {
			this.out = out;
			cacheOut = new ByteArrayOutputStream(initialCachedSize);
		}

		@Override
		public void write(int b) throws IOException {
			out.write(b);
			cacheOut.write(b);
		}

		@Override
		public void write(byte[] b) throws IOException {
			out.write(b);
			cacheOut.write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			out.write(b, off, len);
			cacheOut.write(b, off, len);
		}

		@Override
		public void flush() throws IOException {
			out.flush();
		}

		@Override
		public void close() throws IOException {
			out.close();
		}

		public CachedFilePayload getCachedPayloadObject() {
			CachedFilePayload cachedPayload = new CachedFilePayload();
			cachedPayload.payload = cacheOut.toByteArray();

			return cachedPayload;
		}

	}

	private void tapPayloadIfApplicable(WebzFile file, final OutputStream out) throws IOException, WebzException {
		long numberOfBytes = file.getFileSpecific().getNumberOfBytes();

		if (numberOfBytes > filePayloadSizeThreshold) {

			// ~
			super._fileContentToOutputStream(file, out);
			// ~

		} else {
			CacheTappingOutputStream tappingStream = new CacheTappingOutputStream(out, (int) numberOfBytes);

			// ~
			super._fileContentToOutputStream(file, tappingStream);
			// ~

			CachedFilePayload cachedPayload = tappingStream.getCachedPayloadObject();
			cachedPayload.revision = file.getFileSpecific().getRevision();

			cachedPayloads.put(file.getPathName(), new SoftReference<>(cachedPayload));
		}
	}

	private WebzFile invalidateCachedPayload(WebzFile file) throws IOException, WebzException {
		SoftReference<CachedFilePayload> oldSoftRef = cachedPayloads.remove(file.getPathName());
		if (oldSoftRef != null) {
			oldSoftRef.clear();
		}
		return file;
	}

	@Override
	public WebzFile _fileContentToOutputStream(WebzFile file, OutputStream out) throws IOException, WebzException {

		if (file.exits() && file.isFile()) {

			SoftReference<CachedFilePayload> cachedPayloadRef = cachedPayloads.get(file.getPathName());
			CachedFilePayload cachedPayload = cachedPayloadRef == null ? null : cachedPayloadRef.get();

			if (cachedPayload != null && StringUtils.equals(cachedPayload.revision, file.getFileSpecific().getRevision())) {

				IOUtils.write(cachedPayload.payload, out);

			} else {
				tapPayloadIfApplicable(file, out);
			}
		} else {
			invalidateCachedPayload(file);

			// ~
			super._fileContentToOutputStream(file, out);
			// ~

		}

		return file;
	}

	@Override
	public WebzFile _uploadFile(WebzFile file, byte[] content) throws IOException, WebzException {
		super._uploadFile(file, content);
		return invalidateCachedPayload(file);
	}

	@Override
	public WebzFile _move(WebzFile srcFile, WebzFile destFile) throws IOException, WebzException {
		super._move(srcFile, destFile);
		return invalidateCachedPayload(destFile);
	}

	@Override
	public WebzFile _copy(WebzFile srcFile, WebzFile destFile) throws IOException, WebzException {
		super._copy(srcFile, destFile);
		return invalidateCachedPayload(destFile);
	}

	@Override
	public void _delete(WebzFile file) throws IOException, WebzException {
		super._delete(file);
		invalidateCachedPayload(file);
	}

}
