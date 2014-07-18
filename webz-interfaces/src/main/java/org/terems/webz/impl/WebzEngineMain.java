package org.terems.webz.impl;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzEngine;
import org.terems.webz.WebzException;
import org.terems.webz.impl.cache.ehcache.EhcacheFileSystemCache;
import org.terems.webz.impl.dropbox.DropboxFileSystem;
import org.terems.webz.internal.WebzFileSystem;
import org.terems.webz.obsolete.ObsoleteWebzEngine;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxRequestConfig;

public class WebzEngineMain implements WebzEngine {

	private static Logger LOG = LoggerFactory.getLogger(WebzEngineMain.class);

	private static final DbxRequestConfig DBX_CONFIG = new DbxRequestConfig("webz/0.1", Locale.getDefault().toString());
	// , GaeHttpRequestor.INSTANCE /* for google app engine */ );

	private ObsoleteWebzEngine obsoleteWebzEngine;

	public WebzEngineMain(String rootDropboxPath, String rootDropboxAccessToken) throws WebzException {
		WebzFileSystem dropboxFileSource = new DropboxFileSystem(new DbxClient(DBX_CONFIG, rootDropboxAccessToken),
				rootDropboxPath);

		obsoleteWebzEngine = new ObsoleteWebzEngine(new EhcacheFileSystemCache(dropboxFileSource));
	}

	@Override
	public void fulfilRequest(HttpServletRequest req, HttpServletResponse resp) {
		if (LOG.isTraceEnabled()) {
			LOG.trace("\n\n\n****************************************************************************************************"
					+ "\n***  SERVING "
					+ getFullURL(req)
					+ "\n****************************************************************************************************\n\n");
		}
		obsoleteWebzEngine.fulfilRequest(req, resp);
	}

	private String getFullURL(HttpServletRequest request) {
		StringBuffer requestURL = request.getRequestURL();
		String queryString = request.getQueryString();

		if (queryString == null) {
			return requestURL.toString();
		} else {
			return requestURL.append('?').append(queryString).toString();
		}
	}

}
