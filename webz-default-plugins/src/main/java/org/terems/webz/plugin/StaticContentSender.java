package org.terems.webz.plugin;

import java.io.IOException;

import javax.servlet.ServletResponse;

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

public class StaticContentSender {

	private static final Logger LOG = LoggerFactory.getLogger(StaticContentSender.class);

	private MimetypesConfig mimetypes;
	private String defaultMimetype;

	public StaticContentSender(WebzConfig config) throws WebzException {
		mimetypes = config.getAppConfigObject(MimetypesConfig.class);
		defaultMimetype = config.getAppConfigObject(GeneralAppConfig.class).getDefaultMimetype();
	}

	public WebzMetadata.FileSpecific serveStaticContent(ServletResponse resp, WebzFile content) throws IOException, WebzException {

		WebzFileDownloader downloader = content.getFileDownloader();
		if (downloader == null) {
			return null;
		}
		WebzMetadata.FileSpecific fileSpecific = downloader.fileSpecific;

		resp.setContentType(mimetypes.getMimetype(fileSpecific, defaultMimetype));
		resp.setContentLengthLong(fileSpecific.getNumberOfBytes());

		try {
			downloader.copyContentAndClose(resp.getOutputStream());
		} catch (WebzWriteException e) {
			LOG.debug("most likely client dropped connection while receiving static content", e);
		}

		return fileSpecific;
	}

}
