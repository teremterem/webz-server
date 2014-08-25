package org.terems.webz.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzContext;
import org.terems.webz.WebzContextProxy;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.plugin.BaseWebzFilter;

public class FolderPreloadWelcomeFilter extends BaseWebzFilter {

	private Collection<String> defaultFileExtensions = Arrays.asList(new String[] { ".html" });
	private Collection<String> defaultFileNames = Arrays.asList(new String[] { "index" });

	@Override
	public void service(HttpServletRequest req, HttpServletResponse resp, final WebzChainContext chainContext) throws IOException,
			WebzException {

		chainContext.nextPlease(req, resp, new WebzContextProxy() {

			@Override
			public WebzFile resolveFile(HttpServletRequest req) throws IOException, WebzException {

				String pathInfo = req.getPathInfo();
				WebzFile file = super.getFile(pathInfo); // TODO sure ?

				// TODO

				if (pathInfo != null) {

					int lastSlashPos = pathInfo.lastIndexOf(pathInfo);
					if (lastSlashPos < 0) {

						// TODO

					}
				}

				// TODO

				return file;
			}

			@Override
			protected WebzContext getInnerContext() {
				return chainContext;
			}
		});

	}

}
