package org.terems.webz.base;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.terems.webz.WebzChainContext;
import org.terems.webz.WebzContext;
import org.terems.webz.WebzException;
import org.terems.webz.util.WebzUtils;

/** TODO !!! describe !!! **/
public abstract class BaseLastModifiedWebzFilter<R> extends BaseWebzFilter {

	/** TODO !!! describe !!! **/
	protected abstract R resolveResource(HttpServletRequest req, WebzContext context) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	protected abstract Long resolveLastModified(R resource) throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	protected abstract void serveResource(R resource, HttpServletRequest req, HttpServletResponse resp, WebzChainContext chainContext)
			throws IOException, WebzException;

	/** TODO !!! describe !!! **/
	protected void serveNotFound(HttpServletRequest req, HttpServletResponse resp, WebzChainContext chainContext) throws IOException,
			WebzException {
		// resource does not exist - invoke the next filter in the chain (filter chain usually ends with NotFoundFilter)
		chainContext.nextPlease(req, resp);
	}

	@Override
	public void serve(HttpServletRequest req, HttpServletResponse resp, WebzChainContext chainContext) throws IOException, WebzException {

		R resource = resolveResource(req, chainContext);

		if (resource == null) {
			serveNotFound(req, resp, chainContext);
			return;
		}

		if (WebzUtils.isHttpMethodGet(req) || WebzUtils.isHttpMethodHead(req)) {

			Long lastModifiedPrecise = resolveLastModified(resource);
			if (lastModifiedPrecise == null || lastModifiedPrecise < 0) {

				// this particular resource doesn't support last modified time
				serveResource(resource, req, resp, chainContext);

			} else {

				long modifiedSince = req.getDateHeader(HEADER_IF_MODIFIED_SINCE);
				long lastModified = roundUpLastModified(lastModifiedPrecise);
				if (modifiedSince < lastModified) {

					// resource has changed - serve it (also, modifiedSince of value -1 will always be less than lastModified)
					setLastModifiedIfApplicable(resp, lastModified);
					serveResource(resource, req, resp, chainContext);

				} else {
					resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				}
			}

		} else {
			serveResource(resource, req, resp, chainContext);
		}
	}

	private Long roundUpLastModified(Long lastModified) {

		if (lastModified == null) {
			return null;
		}
		if (lastModified > 0) {
			return lastModified / 1000 * 1000;
		}
		return lastModified;
	}

	private void setLastModifiedIfApplicable(HttpServletResponse resp, Long lastModified) {

		if (!resp.containsHeader(HEADER_LAST_MODIFIED) && lastModified != null && lastModified >= 0) {
			resp.setDateHeader(HEADER_LAST_MODIFIED, lastModified);
		}
	}

}
