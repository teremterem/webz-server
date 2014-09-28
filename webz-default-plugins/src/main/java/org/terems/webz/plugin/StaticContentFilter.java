package org.terems.webz.plugin;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzContext;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzMetadata;
import org.terems.webz.plugin.base.BaseLastModifiedWebzFilter;
import org.terems.webz.plugin.base.BaseWebzFilter;
import org.terems.webz.util.WebzUtils;

public class StaticContentFilter extends BaseLastModifiedWebzFilter<WebzFile> {

	private static final Logger LOG = LoggerFactory.getLogger(StaticContentFilter.class);

	private StaticContentSender contentSender;

	@Override
	public void init() throws WebzException {
		contentSender = new StaticContentSender(getAppConfig());
	}

	@Override
	protected WebzFile resolveResource(HttpServletRequest req, WebzContext context) throws IOException, WebzException {

		WebzFile file = context.resolveFile(req);

		WebzMetadata metadata = file.getMetadata();
		if (metadata == null || !metadata.isFile()) {
			return null;
		}

		return file;
	}

	@Override
	protected Long resolveLastModified(WebzFile resource) throws IOException, WebzException {

		Date lastModifiedDate = resource.getMetadata().getFileSpecific().getLastModified();
		Long lastModified = lastModifiedDate == null ? null : lastModifiedDate.getTime();

		if (LOG.isTraceEnabled()) {
			LOG.trace("\n\n  " + resource + "\n  " + BaseWebzFilter.HEADER_LAST_MODIFIED + ": "
					+ (lastModified == null ? "UNDEFINED !!!" : WebzUtils.formatHttpDate(lastModified)) + "\n");
		}
		return lastModified;
	}

	@Override
	protected void serveResource(WebzFile resource, HttpServletRequest req, HttpServletResponse resp, WebzChainContext chainContext)
			throws IOException, WebzException {

		WebzMetadata.FileSpecific fileSpecific = contentSender.serveStaticContent(resp, resource);

		if (fileSpecific == null) {
			// file does not exist or is not a file - invoke the next filter in the chain (filter chain usually ends with NotFoundFilter)
			chainContext.nextPlease(req, resp);
		}
	}

}
