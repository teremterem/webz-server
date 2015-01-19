package org.terems.webz.plugin;

import java.io.IOException;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzConfig;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileDownloader;
import org.terems.webz.WebzMetadata;
import org.terems.webz.WebzWriteException;
import org.terems.webz.config.GeneralAppConfig;
import org.terems.webz.config.MimetypesConfig;
import org.terems.webz.util.WebzUtils;

public class StaticContentSender {

	public static final ByteOrderMark[] ALL_BOMS = { ByteOrderMark.UTF_8, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_16LE,
			ByteOrderMark.UTF_32BE, ByteOrderMark.UTF_32LE };

	private static final Logger LOG = LoggerFactory.getLogger(StaticContentSender.class);

	private MimetypesConfig mimetypes;
	private String defaultMimetype;
	private String defaultEncoding;

	public StaticContentSender(WebzConfig config) throws WebzException {

		mimetypes = config.getAppConfigObject(MimetypesConfig.class);

		GeneralAppConfig appConfig = config.getAppConfigObject(GeneralAppConfig.class);
		defaultMimetype = appConfig.getDefaultMimetype();
		defaultEncoding = appConfig.getDefaultEncoding();
	}

	public WebzMetadata.FileSpecific serveStaticContent(HttpServletRequest req, ServletResponse resp, WebzFile content) throws IOException,
			WebzException {

		WebzFileDownloader downloader = content.getFileDownloader();
		if (downloader == null) {
			return null;
		}
		WebzMetadata.FileSpecific fileSpecific = downloader.fileSpecific;
		if (fileSpecific == null) {
			return null;
		}

		@SuppressWarnings("resource")
		BOMInputStream bomIn = new BOMInputStream(downloader.content, false, ALL_BOMS);
		downloader.content = bomIn;

		String encoding = defaultEncoding;
		long contentLength = fileSpecific.getNumberOfBytes();

		ByteOrderMark bom = bomIn.getBOM();
		if (bom != null) {
			encoding = bom.getCharsetName();
			contentLength -= bom.length();
		}
		resp.setContentType(mimetypes.getMimetype(fileSpecific, defaultMimetype));
		resp.setCharacterEncoding(encoding);
		resp.setContentLengthLong(contentLength);

		if (WebzUtils.isHttpMethodHead(req)) {
			downloader.close();
		} else {
			try {
				downloader.copyContentAndClose(resp.getOutputStream());

			} catch (WebzWriteException e) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("most likely client dropped connection while receiving static content from " + content, e);
				}
			}
		}
		resp.flushBuffer();

		return fileSpecific;
	}

}
