package org.terems.webz;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebzEngine {

	private static Logger LOG = LoggerFactory.getLogger(WebzEngine.class);

	private WebzFileSource fileSource;

	private Properties mimetypes = new Properties();
	private Properties domains = new Properties();
	private Properties baseAuth = new Properties();

	public WebzEngine(WebzFileSource fileSource) throws WebzException {
		this.fileSource = fileSource;
		initMimetypes();
	}

	private void initMimetypes() throws WebzException {

		// TODO implement properties refresh mechanism (for ex. based on properties files update time)

		try {
			int defaultBufSizeForProps = 2048;
			mimetypes
					.load(new ByteArrayInputStream(fileSource.absorbFile(WebzConstants.MIMETYPES_FILE, defaultBufSizeForProps)));
			domains.load(new ByteArrayInputStream(fileSource.absorbFile(WebzConstants.DOMAINS_FILE, defaultBufSizeForProps)));
			baseAuth.load(new ByteArrayInputStream(fileSource.absorbFile(WebzConstants.BASE_AUTH_FILE, defaultBufSizeForProps)));
		} catch (IOException e) {
			throw new WebzException(e.getMessage(), e);
		}
	}

	public void fulfilRequest(HttpServletRequest req, HttpServletResponse resp) {
		if (baseAuthenticationNeeded(req)) {
			if (proceedWithBaseAuthentication(req)) {
				fulfilRequest0(req, resp);
			} else {
				replyWithUnauthorized(resp);
			}
		} else {
			fulfilRequest0(req, resp);
		}
	}

	private void replyWithUnauthorized(HttpServletResponse resp) {
		String realm = baseAuth.getProperty(WebzConstants.REALM_PROPERTY);
		if (realm == null) {
			realm = "";
		}
		resp.addHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
		replyWithStatusCodeSafely(resp, HttpServletResponse.SC_UNAUTHORIZED);
	}

	private boolean baseAuthenticationNeeded(HttpServletRequest req) {
		return baseAuth.containsKey(WebzConstants.PASSWORD_PROPERTY);
	}

	private boolean proceedWithBaseAuthentication(HttpServletRequest req) {
		String authHeader = req.getHeader("Authorization");
		if (authHeader != null) {
			StringTokenizer st = new StringTokenizer(authHeader);
			if (st.hasMoreTokens()) {
				String basic = st.nextToken();

				if (basic.equalsIgnoreCase("Basic")) {
					try {
						String credentials = new String(Base64.decodeBase64(st.nextToken()), "utf8");
						if (LOG.isTraceEnabled()) {
							LOG.trace("");
							LOG.trace("");
							LOG.trace("Credentials: " + credentials);
						}
						int colonPos = credentials.indexOf(":");
						if (colonPos > -1) {
							String username = credentials.substring(0, colonPos).trim();
							String password = credentials.substring(colonPos + 1).trim();

							return checkCredentials(username, password);
						} else {
							if (LOG.isTraceEnabled()) {
								LOG.trace("");
								LOG.trace("Invalid authentication token: should consist of username and password separated colon and be base64-encoded");
							}
						}
					} catch (UnsupportedEncodingException e) {
						// should not happen
						throw new RuntimeException("Couldn't retrieve authentication: " + e.getMessage(), e);
					}
				}
			}
		}
		return false;
	}

	private boolean checkCredentials(String username, String password) {
		String correctUsername = baseAuth.getProperty(WebzConstants.USERNAME_PROPERTY);
		if (correctUsername == null) {
			correctUsername = "";
		}
		return username.equals(correctUsername) && password.equals(baseAuth.getProperty(WebzConstants.PASSWORD_PROPERTY));
	}

	private void fulfilRequest0(HttpServletRequest req, HttpServletResponse resp) {
		String pathName = req.getPathInfo();
		if (pathName == null) {
			pathName = "";
		} else {
			pathName = trimWithFileSeparators(pathName);
		}
		String originalPathName = pathName; // reserving path name before domain subfolder operations

		if (LOG.isTraceEnabled()) {
			LOG.trace("");
			LOG.trace("Http Request Server Name: " + req.getServerName());
		}
		String domainSubfolder = lookupDomainSubfolder(req.getServerName());

		boolean isDomainSubfolder = domainSubfolder != null;
		if (isDomainSubfolder) {
			if (domainSubfolder.length() > 0 && pathName.length() > 0) {
				pathName = "/" + pathName;
			}
			pathName = domainSubfolder + pathName;
		}

		try {
			boolean found = populateResponse(pathName, resp, !isDomainSubfolder);
			if (!found && isDomainSubfolder) {
				if (LOG.isTraceEnabled()) {
					LOG.trace("");
					LOG.trace("##### !!! ATTENTION !!! ##### !!! MULTI-DOMAIN SUPPORT !!! ##### !!! FAILOVER FROM " + pathName
							+ " BACK TO " + originalPathName + " !!! #####");
				}
				populateResponse(originalPathName, resp, true);
			}
		} catch (Throwable originalException) {

			if (originalException instanceof IOException) {
				// if IOException then the problem is with response output stream (such exceptions happen if for ex. web browser
				// was closed before receiving complete response body)
				if (LOG.isTraceEnabled()) {
					LOG.trace(originalException.getMessage(), originalException);
				}
			} else {
				LOG.error(originalException.getMessage(), originalException);
			}

			if (!resp.isCommitted()) {
				replyWithStatusCodeSafely(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
	}

	private void replyWithStatusCodeSafely(HttpServletResponse resp, int statusCode) {
		resp.setStatus(statusCode);
		try {
			populateResponse(WebzConstants.AUX_FILES_PREFIX + statusCode, resp, false);
		} catch (Throwable th) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("FAILED TO RENDER PAYLOAD FOR " + statusCode + " STATUS CODE: " + th.getMessage(), th);
			}
		}
	}

	private boolean populateResponse(String pathName, HttpServletResponse resp, boolean populateNotFound) throws WebzException,
			IOException {
		WebzFileMetadata metadata = fileSource.getMetadata(pathName);
		if (metadata == null) {
			if (!pathName.toLowerCase().endsWith(WebzConstants.HTML_SUFFIX)) {
				return populateResponse(pathName + WebzConstants.HTML_SUFFIX, resp, populateNotFound);
			} else {
				if (LOG.isTraceEnabled()) {
					LOG.trace("");
					LOG.trace("******************************************************************************");
					LOG.trace("* Path not found: " + pathName);
					LOG.trace("******************************************************************************");
				}

				if (populateNotFound) {
					resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
					populateResponse(WebzConstants.AUX_FILES_PREFIX + HttpServletResponse.SC_NOT_FOUND, resp, false);
				}
				return false;
			}
		} else {
			if (metadata.isFile()) {
				if (LOG.isTraceEnabled()) {
					LOG.trace("");
					LOG.trace("******************************************************************************");
					LOG.trace("* path / name:    " + pathName);
				}

				String mimetype = lookupMimetype(pathName);

				if (LOG.isTraceEnabled()) {
					LOG.trace("******************************************************************************");
				}

				resp.setContentType(mimetype);
				fileSource.getFile(pathName, resp.getOutputStream());
				return true;
			} else {
				if (LOG.isTraceEnabled()) {
					LOG.trace("");
					LOG.trace("!!! Folder Name from Metadata: " + metadata.getName());
					LOG.trace("!!! Path Name: " + pathName);
				}

				String welcomePath = pathName;
				if (!(welcomePath.length() <= 0 || welcomePath.endsWith("/") || welcomePath.endsWith("\\"))) {
					welcomePath += '/';
				}

				boolean traditionalWelcome = populateResponse(welcomePath + WebzConstants.WELCOME_FILE, resp, false);
				if (!traditionalWelcome) {
					return populateResponse(welcomePath + metadata.getName(), resp, populateNotFound);
				}
				return true;
			}
		}
	}

	private String lookupMimetype(String pathName) {

		// TODO optimize

		String mimetype = WebzConstants.DOMAINS_FILE;
		boolean contentTypeKnown = false;
		for (Map.Entry<Object, Object> entry : mimetypes.entrySet()) {
			if (pathName.toLowerCase().endsWith(entry.getKey().toString().toLowerCase())) {
				mimetype = entry.getValue().toString();
				contentTypeKnown = true;
				break;
			}
		}
		if (LOG.isTraceEnabled()) {
			LOG.trace("* mimetype:       " + mimetype + (contentTypeKnown ? "" : " (default)"));
		}
		return mimetype;
	}

	private String lookupDomainSubfolder(String domain) {

		// TODO optimize

		String subfolder = null;
		for (Map.Entry<Object, Object> entry : domains.entrySet()) {
			if (domain.toLowerCase().endsWith(entry.getKey().toString().toLowerCase())) {
				subfolder = entry.getValue().toString();
				break;
			}
		}

		if (LOG.isTraceEnabled()) {
			if (subfolder != null) {
				LOG.trace("Domain Subfolder:       " + subfolder);
			}
		}

		if (subfolder == null) {
			return null;
		} else {
			return trimWithFileSeparators(subfolder);
		}
	}

	private String trimWithFileSeparators(String path) {
		path = path.trim();
		if (path.startsWith("/") || path.startsWith("\\")) {
			path = path.substring(1);
		}
		if (path.endsWith("/") || path.endsWith("\\")) {
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}

}
