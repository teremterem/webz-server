package org.terems.webz.obsolete.dropbox.gae;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

import com.dropbox.core.http.HttpRequestor;

public class GaeHttpRequestor extends HttpRequestor {

	private final Proxy proxy;

	public GaeHttpRequestor() {
		this(Proxy.NO_PROXY);
	}

	public GaeHttpRequestor(Proxy proxy) {
		this.proxy = proxy;
	}

	public static final int DefaultConnectTimeoutMillis = 25 * 1000;

	public static final int DefaultReadTimeoutMillis = 25 * 1000;

	public static final GaeHttpRequestor INSTANCE = new GaeHttpRequestor();

	private static Response toResponse(HttpURLConnection conn) throws IOException {
		int responseCode = conn.getResponseCode();
		InputStream bodyStream;
		if (responseCode >= 400) {
			bodyStream = conn.getErrorStream();
		} else {
			bodyStream = conn.getInputStream();
		}
		return new Response(conn.getResponseCode(), bodyStream, conn.getHeaderFields());
	}

	@Override
	public Response doGet(String url, Iterable<Header> headers) throws IOException {
		HttpURLConnection conn = prepRequest(url, headers);
		conn.setRequestMethod("GET");
		conn.connect();
		return toResponse(conn);
	}

	@Override
	public Uploader startPost(String url, Iterable<Header> headers) throws IOException {
		HttpURLConnection conn = prepRequest(url, headers);
		conn.setRequestMethod("POST");
		return new Uploader(conn);
	}

	@Override
	public Uploader startPut(String url, Iterable<Header> headers) throws IOException {
		HttpURLConnection conn = prepRequest(url, headers);
		conn.setRequestMethod("PUT");
		return new Uploader(conn);
	}

	protected void configureConnection(HttpURLConnection conn) throws IOException {
	}

	private static class Uploader extends HttpRequestor.Uploader {
		private HttpURLConnection conn;

		public Uploader(HttpURLConnection conn) throws IOException {
			super(getOutputStream(conn));
			conn.connect();
			this.conn = conn;
		}

		private static OutputStream getOutputStream(HttpURLConnection conn) throws IOException {
			conn.setDoOutput(true);
			return conn.getOutputStream();
		}

		@Override
		public void abort() {
			if (conn == null) {
				throw new IllegalStateException("Can't abort().  Uploader already closed.");
			}
			this.conn.disconnect();
		}

		@Override
		public void close() {
			if (conn == null)
				return;
			this.conn.disconnect();
		}

		@Override
		public Response finish() throws IOException {
			HttpURLConnection conn = this.conn;
			if (conn == null) {
				throw new IllegalStateException("Can't finish().  Uploader already closed.");
			}
			this.conn = null;
			return toResponse(conn);
		}
	}

	private HttpURLConnection prepRequest(String url, Iterable<Header> headers) throws IOException {
		URL urlObject = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) urlObject.openConnection(this.proxy);

		// SSLConfig.apply(conn);
		conn.setConnectTimeout(DefaultConnectTimeoutMillis);
		conn.setReadTimeout(DefaultReadTimeoutMillis);
		conn.setUseCaches(false);
		conn.setAllowUserInteraction(false);

		configureConnection(conn);

		for (Header header : headers) {
			conn.addRequestProperty(header.key, header.value);
		}

		return conn;
	}

}
