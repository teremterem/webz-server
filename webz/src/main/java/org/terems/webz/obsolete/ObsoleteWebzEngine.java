package org.terems.webz.obsolete;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import org.eclipse.mylyn.wikitext.core.util.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;
import org.terems.webz.WebzFileFactory;
import org.terems.webz.WebzFileMetadata;
import org.terems.webz.impl.GenericWebzFile;

@Deprecated
public class ObsoleteWebzEngine {

	private static Logger LOG = LoggerFactory.getLogger(ObsoleteWebzEngine.class);

	private WebzFileFactory fileFactory;

	private Properties mimetypes = new Properties();
	private Properties domains = new Properties();
	private Properties general = new Properties();
	private Properties wikitexts = new Properties();

	private String baseauthRealm;
	private String baseauthUsername;
	private String baseauthPassword;

	private String[] defaultFileSuffixesPrioritized;
	private String defaultMimetype;
	private String lastResortWelcomeFile;

	public ObsoleteWebzEngine(WebzFileFactory fileFactory) throws WebzException {
		this.fileFactory = fileFactory;
		initMimetypes();
	}

	private void initMimetypes() throws WebzException {

		// TODO implement properties refresh mechanism (for ex. based on properties files update time)

		try {
			mimetypes.load(new ByteArrayInputStream(fileFactory.get(ObsoleteWebzConstants._MIMETYPES_PROPERTIES_FILE)
					.getFileContent(ObsoleteWebzConstants.DEFAULT_BUF_SIZE)));
			domains.load(new ByteArrayInputStream(fileFactory.get(ObsoleteWebzConstants._DOMAINS_PROPERTIES_FILE)
					.getFileContent(ObsoleteWebzConstants.DEFAULT_BUF_SIZE)));
			general.load(new ByteArrayInputStream(fileFactory.get(ObsoleteWebzConstants._GENERAL_PROPERTIES_FILE)
					.getFileContent(ObsoleteWebzConstants.DEFAULT_BUF_SIZE)));
			wikitexts.load(new ByteArrayInputStream(fileFactory.get(ObsoleteWebzConstants._WIKITEXTS_PROPERTIES_FILE)
					.getFileContent(ObsoleteWebzConstants.DEFAULT_BUF_SIZE)));

			baseauthRealm = general.getProperty(ObsoleteWebzConstants.BASEAUTH_REALM_PROPERTY, "");
			baseauthUsername = general.getProperty(ObsoleteWebzConstants.BASEAUTH_USERNAME_PROPERTY, "");
			baseauthPassword = general.getProperty(ObsoleteWebzConstants.BASEAUTH_PASSWORD_PROPERTY);

			defaultFileSuffixesPrioritized = general.getProperty(ObsoleteWebzConstants.DEFAULT_FILE_SUFFIXES_PROPERTY, ".html")
					.split("\\s*,\\s*");
			for (int i = 0; i < defaultFileSuffixesPrioritized.length; i++) {
				defaultFileSuffixesPrioritized[i] = defaultFileSuffixesPrioritized[i].trim().toLowerCase();
			}

			defaultMimetype = general.getProperty(ObsoleteWebzConstants.DEFAULT_MIMETYPE_PROPERTY,
					ObsoleteWebzConstants.DEFAULT_MIMETYPE);
			lastResortWelcomeFile = general.getProperty(ObsoleteWebzConstants.LAST_RESORT_WELCOME_FILE_PROPERTY,
					ObsoleteWebzConstants.DEFAULT_LAST_RESORT_WELCOME_FILE);

		} catch (IOException e) {
			throw new WebzException(e.getMessage(), e);
		}
	}

	public void fulfilRequest(HttpServletRequest req, HttpServletResponse resp) {
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
		// TODO ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ TODO ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ TODO \\
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
		if (LOG.isTraceEnabled()) {
			LOG.trace("");
			LOG.trace("**************************************************");
			Enumeration<Locale> locales = req.getLocales();
			while (locales.hasMoreElements()) {
				Locale locale = locales.nextElement();
				LOG.trace(locale.toLanguageTag());
			}
			LOG.trace("**************************************************");
			LOG.trace("");
		}

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
		// TODO ~~~~~~~ SEPARATE HTTP PASSWORD FOR EVERY DOMAIN: ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ TODO \\
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
		if (baseAuthenticationNeeded(req)) {
			if (proceedWithBaseAuthentication(req)) {
				fulfilRequest0(req, resp);
			} else {
				replyWithUnauthorized(req, resp);
			}
		} else {
			fulfilRequest0(req, resp);
		}
	}

	private void replyWithUnauthorized(HttpServletRequest req, HttpServletResponse resp) {
		resp.addHeader("WWW-Authenticate", "Basic realm=\"" + baseauthRealm + "\"");
		replyWithStatusCodeSafely(req, resp, HttpServletResponse.SC_UNAUTHORIZED);
	}

	private boolean baseAuthenticationNeeded(HttpServletRequest req) {
		return general.containsKey(ObsoleteWebzConstants.BASEAUTH_PASSWORD_PROPERTY);
	}

	private boolean proceedWithBaseAuthentication(HttpServletRequest req) {
		String authHeader = req.getHeader("Authorization");
		if (authHeader != null) {
			StringTokenizer st = new StringTokenizer(authHeader);
			if (st.hasMoreTokens()) {
				String basic = st.nextToken();

				if (basic.equalsIgnoreCase("Basic")) {
					try {
						String credentials = new String(Base64.decodeBase64(st.nextToken()),
								ObsoleteWebzConstants.DEFAULT_ENCODING);
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

	private boolean checkCredentials(String usernameEntered, String passwordEntered) {
		boolean accepted = usernameEntered.equals(baseauthUsername) && passwordEntered.equals(baseauthPassword);
		if (LOG.isTraceEnabled()) {
			LOG.trace("");
			LOG.trace("");
			LOG.trace("Credentials - " + usernameEntered + ":***** - " + (accepted ? "Accepted." : "REJECTED !!"));
		}
		return accepted;
	}

	private void fulfilRequest0(HttpServletRequest req, HttpServletResponse resp) {
		String pathName = req.getPathInfo();
		if (pathName == null) {
			pathName = "";
		} else {
			pathName = GenericWebzFile.trimFileSeparators(pathName);
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
			boolean found = populateResponse(pathName, req, resp, !isDomainSubfolder, true);
			if (!found && isDomainSubfolder) {
				if (LOG.isTraceEnabled()) {
					LOG.trace("");
					LOG.trace("##### !!! ATTENTION !!! ##### !!! MULTI-DOMAIN SUPPORT !!! ##### !!! FAILOVER FROM " + pathName
							+ " BACK TO " + originalPathName + " !!! #####");
				}
				populateResponse(originalPathName, req, resp, true, true);
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
				replyWithStatusCodeSafely(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
	}

	private void replyWithStatusCodeSafely(HttpServletRequest req, HttpServletResponse resp, int statusCode) {
		resp.setStatus(statusCode);
		try {
			populateResponse(ObsoleteWebzConstants.AUX_FILES_PREFIX + statusCode, req, resp, false, true);
		} catch (Throwable th) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("FAILED TO RENDER PAYLOAD FOR " + statusCode + " STATUS CODE: " + th.getMessage(), th);
			}
		}
	}

	/**
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * Main <code>populateResponse()</code> function. May potentially call itself internally.
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 */
	private boolean populateResponse(String pathName, HttpServletRequest req, HttpServletResponse resp,
			boolean populateNotFound, boolean tryStandardSuffixes) throws WebzException, IOException {
		WebzFile file = fileFactory.get(pathName);
		WebzFileMetadata fileMetadata = file.getMetadata();
		if (fileMetadata == null) {
			if (tryStandardSuffixes) {
				return tryWithOneOfStandardSuffixes(pathName, req, resp, populateNotFound);
			} else {
				populateNotFoundResponseIfNecessary(req, resp, populateNotFound);
				return false;
			}
		} else {
			if (fileMetadata.isFile()) {
				return populateResponseFromFile(file, req, resp);
			} else {
				if (LOG.isTraceEnabled()) {
					LOG.trace("");
					LOG.trace("!!! Folder Name from Metadata: " + fileMetadata.getName());
					LOG.trace("!!! Path Name: " + pathName);
				}

				String welcomePath = pathName;
				if (!(welcomePath.length() <= 0 || welcomePath.endsWith("/") || welcomePath.endsWith("\\"))) {
					welcomePath += '/';
				}

				boolean traditionalWelcome = populateResponse(welcomePath + lastResortWelcomeFile, req, resp, false,
						tryStandardSuffixes);
				if (!traditionalWelcome) {
					return populateResponse(welcomePath + fileMetadata.getName(), req, resp, populateNotFound,
							tryStandardSuffixes);
				}
				return true;
			}
		}
	}

	private boolean tryWithOneOfStandardSuffixes(String pathName, HttpServletRequest req, HttpServletResponse resp,
			boolean populateNotFound) throws WebzException, IOException {
		boolean alreadyStandardSuffix = false;
		for (String defaultSuffix : defaultFileSuffixesPrioritized) {
			if (pathName.toLowerCase().endsWith(defaultSuffix)) {
				alreadyStandardSuffix = true;
				break;
			}
		}
		if (alreadyStandardSuffix) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("");
				LOG.trace("******************************************************************************");
				LOG.trace("* Path not found: " + pathName);
				LOG.trace("******************************************************************************");
			}

			populateNotFoundResponseIfNecessary(req, resp, populateNotFound);
			return false;
		} else {
			for (String defaultSuffix : defaultFileSuffixesPrioritized) {
				if (populateResponse(pathName + defaultSuffix, req, resp, false, false)) {
					return true;
				}
			}

			populateNotFoundResponseIfNecessary(req, resp, populateNotFound);
			return false;
		}

	}

	/**
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * Reads a file from (abstract) <filesystem>filesystem</filesystem>, processes it and populates <code>resp</code> with 200
	 * "OK" response and payload.
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 */
	private boolean populateResponseFromFile(WebzFile file, HttpServletRequest req, HttpServletResponse resp)
			throws IOException, WebzException {

		if (LOG.isTraceEnabled()) {
			LOG.trace("");
			LOG.trace("******************************************************************************");
			LOG.trace("* path / name:    " + file.getPathName());
		}

		String mimetype = null;
		String wikitextPropertiesFile = lookupInWikitexts(file.getPathName());
		Properties wikitextProperties = null;

		if (wikitextPropertiesFile != null) {
			wikitextPropertiesFile = GenericWebzFile.trimFileSeparators(wikitextPropertiesFile);

			wikitextProperties = new Properties();
			wikitextProperties.load(new ByteArrayInputStream(fileFactory.get(wikitextPropertiesFile).getFileContent(
					ObsoleteWebzConstants.DEFAULT_BUF_SIZE)));

			mimetype = wikitextProperties.getProperty(ObsoleteWebzConstants.MIMETYPE_PROPERTY);
		}

		if (mimetype == null) {
			mimetype = lookupMimetype(file.getPathName());
		}

		if (LOG.isTraceEnabled()) {
			LOG.trace("******************************************************************************");
		}

		resp.setContentType(mimetype);
		if (wikitextProperties == null) {

			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
			// TODO ~~~~~~ WHAT TO DO WITH "BYTE ORDER MARK" (BOM) IF PRESENT IN STATIC CONTENT? ~~~~~~ TODO \\
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
			file.fileContentToOutputStream(resp.getOutputStream());

		} else {
			if (req.getParameterMap().containsKey(ObsoleteWebzConstants.EDIT)) {

				editWikitext(file, resp, req.getParameterMap().containsKey(ObsoleteWebzConstants.IGNORE_DRAFT));

			} else if (req.getParameterMap().containsKey(ObsoleteWebzConstants.SAVE_DRAFT)) {

				saveWikitextDraft(file, req);
				resp.sendRedirect(req.getRequestURI() + "?" + ObsoleteWebzConstants.EDIT);

			} else if (req.getParameterMap().containsKey(ObsoleteWebzConstants.PUBLISH)) {

				publishWikitext(file, req);
				resp.sendRedirect(req.getRequestURI() + "?");

			} else if (req.getParameterMap().containsKey(ObsoleteWebzConstants.PREVIEW)) {

				viewWikitext(fileContentAsString(getNewWikitextContent(req)), resp, wikitextPropertiesFile, wikitextProperties);

			} else {

				viewWikitext(fileContentAsString(file.getFileContent()), resp, wikitextPropertiesFile, wikitextProperties);
			}
		}
		return true;
	}

	private byte[] getNewWikitextContent(HttpServletRequest req) throws WebzException, UnsupportedEncodingException {
		String wikitextNewContent = req.getParameter(ObsoleteWebzConstants.WIKITEXT_INPUT_NAME);
		if (wikitextNewContent == null) {
			throw new WebzException(ObsoleteWebzConstants.WIKITEXT_INPUT_NAME + " request parameter was not submitted");
		}

		String editPageEncoding = wikitexts.getProperty(ObsoleteWebzConstants.EDIT_PAGE_ENCODING_PROPERTY,
				ObsoleteWebzConstants.DEFAULT_ENCODING);
		return wikitextNewContent.getBytes(editPageEncoding);
	}

	private void editWikitext(WebzFile file, HttpServletResponse resp, boolean ignoreDraft) throws WebzException, IOException,
			UnsupportedEncodingException {

		String draftFilePathName = file.getPathName()
				+ wikitexts.getProperty(ObsoleteWebzConstants.DRAFT_FILE_SUFFIX_PROPERTY,
						ObsoleteWebzConstants.DEFAULT_DRAFT_FILE_SUFFIX);

		boolean showDraft = !ignoreDraft;
		if (showDraft) {
			WebzFile draftFile = fileFactory.get(draftFilePathName);

			WebzFileMetadata draftFileMetadata = draftFile.getMetadata();
			showDraft = draftFileMetadata != null;
			if (showDraft) {
				if (!draftFileMetadata.isFile()) {
					throw new WebzException("'" + draftFilePathName + "' is not a file");
				}

				file = draftFile;
			}
		}

		populateWikitextEditTemplate(resp, file, showDraft, ignoreDraft);
	}

	private void populateWikitextEditTemplate(HttpServletResponse resp, WebzFile file, boolean draftOpened, boolean draftIgnored)
			throws WebzException, IOException, UnsupportedEncodingException {
		String templateFile = wikitexts.getProperty(ObsoleteWebzConstants.EDIT_PAGE_TEMPLATE_PROPERTY);
		if (templateFile == null) {
			throw new WebzException(ObsoleteWebzConstants.EDIT_PAGE_TEMPLATE_PROPERTY + " property is not set in "
					+ ObsoleteWebzConstants._WIKITEXTS_PROPERTIES_FILE);
		}

		String templateString = fileContentAsString(fileFactory.get(templateFile).getFileContent());

		String contentString = fileContentAsString(file.getFileContent());

		String draftOpenedVar = wikitexts.getProperty(ObsoleteWebzConstants.DRAFT_OPENED_VAR_PROPERTY);
		String draftIgnoredVar = wikitexts.getProperty(ObsoleteWebzConstants.DRAFT_IGNORED_VAR_PROPERTY);
		String internalPathVar = wikitexts.getProperty(ObsoleteWebzConstants.EDIT_INTERNAL_PATH_VAR_PROPERTY);
		String textareaContentVar = wikitexts.getProperty(ObsoleteWebzConstants.EDIT_TEXTAREA_CONTENT_VAR_PROPERTY);

		templateString = templateString.replace(draftOpenedVar, String.valueOf(draftOpened));
		templateString = templateString.replace(draftIgnoredVar, String.valueOf(draftIgnored));
		templateString = templateString.replace(internalPathVar, "/" + file.getPathName());
		templateString = templateString.replace(textareaContentVar, StringEscapeUtils.escapeHtml4(contentString));

		String editPageEncoding = wikitexts.getProperty(ObsoleteWebzConstants.EDIT_PAGE_ENCODING_PROPERTY,
				ObsoleteWebzConstants.DEFAULT_ENCODING);
		writeStringToResp(resp, templateString, editPageEncoding);
	}

	private void writeStringToResp(HttpServletResponse resp, String stringToWrite, String outputEncoding)
			throws UnsupportedEncodingException, IOException {
		OutputStreamWriter respWriter = new OutputStreamWriter(resp.getOutputStream(), outputEncoding);
		respWriter.write(stringToWrite);
		respWriter.flush();
	}

	private WebzFile getDraftFile(WebzFile file) throws IOException, WebzException {
		return fileFactory.get(file.getPathName()
				+ wikitexts.getProperty(ObsoleteWebzConstants.DRAFT_FILE_SUFFIX_PROPERTY,
						ObsoleteWebzConstants.DEFAULT_DRAFT_FILE_SUFFIX));
	}

	private void saveWikitextDraft(WebzFile file, HttpServletRequest req) throws IOException, WebzException {
		getDraftFile(file).uploadFile(getNewWikitextContent(req));
	}

	private void publishWikitext(WebzFile file, HttpServletRequest req) throws WebzException, IOException,
			UnsupportedEncodingException {
		file.uploadFile(getNewWikitextContent(req));

		WebzFile draftFile = getDraftFile(file);
		WebzFileMetadata draftFileMetadata = draftFile.getMetadata();
		if (draftFileMetadata != null && draftFileMetadata.isFile()) {
			draftFile.delete();
		}
	}

	private void viewWikitext(String contentString, HttpServletResponse resp, String wikitextPropertiesFile,
			Properties wikitextProperties) throws WebzException, IOException, UnsupportedEncodingException {
		String templateFile = wikitextProperties.getProperty(ObsoleteWebzConstants.TEMPLATE_PROPERTY);
		if (templateFile == null) {
			throw new WebzException(ObsoleteWebzConstants.TEMPLATE_PROPERTY + " property is not set in "
					+ wikitextPropertiesFile);
		}

		String wikitextPropertiesFolder = trimToFolder(wikitextPropertiesFile);
		templateFile = wikitextPropertiesFolder + "/" + GenericWebzFile.trimFileSeparators(templateFile);

		String templateString = fileContentAsString(fileFactory.get(templateFile).getFileContent());

		String outputEncoding = wikitextProperties.getProperty(ObsoleteWebzConstants.OUTPUT_ENCODING_PROPERTY,
				ObsoleteWebzConstants.DEFAULT_ENCODING);
		processWikitext(resp, wikitextPropertiesFolder, wikitextProperties, templateString, contentString, outputEncoding);
	}

	private String fileContentAsString(byte[] fileContent) throws IOException, WebzException {
		StringWriter contentWriter = new StringWriter();

		BOMInputStream bomIn = new BOMInputStream(new ByteArrayInputStream(fileContent), false, ObsoleteWebzConstants.ALL_BOMS);

		String encoding = bomIn.getBOMCharsetName();
		if (encoding == null) {
			encoding = ObsoleteWebzConstants.DEFAULT_ENCODING;
		}

		IOUtils.copy(bomIn, contentWriter, encoding);
		String contentString = contentWriter.toString();

		bomIn.close();
		return contentString;
	}

	private void processWikitext(HttpServletResponse resp, String wikitextPropertiesFolder, Properties wikitextProperties,
			String templateString, String contentString, String outputEncoding) throws WebzException, IOException,
			UnsupportedEncodingException {
		Pattern sectionsRegexp = Pattern.compile(wikitextProperties
				.getProperty(ObsoleteWebzConstants.SECTION_VARS_REGEXP_PROPERTY));
		Matcher templateSectionsMatcher = sectionsRegexp.matcher(templateString);

		Matcher contentSectionsMatcher = sectionsRegexp.matcher(contentString);

		Map<String, String> sectionContentMap = new HashMap<String, String>();

		// ~

		getAllSectionsContent(wikitextProperties, contentString, contentSectionsMatcher, sectionContentMap);

		// ~

		Writer respWriter = new BufferedWriter(new OutputStreamWriter(resp.getOutputStream(), outputEncoding),
				ObsoleteWebzConstants.DEFAULT_BUF_SIZE);

		String defaultLinkRel = wikitextProperties.getProperty(ObsoleteWebzConstants.WIKITEXT_LINK_REL_PROPERTY
				+ ObsoleteWebzConstants.DEFAULT_PROPERTY_SUFFIX);
		String defaultAbsoluteLinkTarget = wikitextProperties
				.getProperty(ObsoleteWebzConstants.WIKITEXT_ABSOLUTE_LINK_TARGET_PROPERTY
						+ ObsoleteWebzConstants.DEFAULT_PROPERTY_SUFFIX);

		Collection<RegexpReplacement> generalRegexpReplacements = extractRegexpReplacements(wikitextProperties,
				wikitextPropertiesFolder, null);

		String defaultLanguage = wikitextProperties.getProperty(ObsoleteWebzConstants.WIKITEXT_LANG_PROPERTY
				+ ObsoleteWebzConstants.DEFAULT_PROPERTY_SUFFIX, ObsoleteWebzConstants.LANGUAGE_RAW);

		int sectionNameRegexpGroup = Integer.parseInt(wikitextProperties
				.getProperty(ObsoleteWebzConstants.SECTION_NAME_REGEXP_GROUP_PROPERTY));

		int lastPos = 0;
		while (templateSectionsMatcher.find()) {

			respWriter.write(templateString.substring(lastPos, templateSectionsMatcher.start()));

			lastPos = templateSectionsMatcher.end();
			String sectionName = templateSectionsMatcher.group(sectionNameRegexpGroup);

			String sectionContent = sectionContentMap.get(sectionName);
			if (sectionContent == null) {

				respWriter.write(templateSectionsMatcher.group());

			} else {

				String language = getSectionLanguage(wikitextProperties, sectionName, defaultLanguage);
				boolean languageRaw = ObsoleteWebzConstants.LANGUAGE_RAW.equals(language);

				boolean regexpsForRaw = Boolean.parseBoolean(wikitextProperties.getProperty(
						ObsoleteWebzConstants.WIKITEXT_REGEXPS_FOR_RAW_PROPERTY,
						ObsoleteWebzConstants.DEFAULT_REGEXPS_FOR_RAW.toString()));

				if (!languageRaw || regexpsForRaw) {
					for (RegexpReplacement replacement : generalRegexpReplacements) {
						sectionContent = sectionContent.replaceAll(replacement.regexp, replacement.replacement);
					}

					Collection<RegexpReplacement> regexpReplacements = extractRegexpReplacements(wikitextProperties,
							wikitextPropertiesFolder, sectionName);
					for (RegexpReplacement replacement : regexpReplacements) {
						sectionContent = sectionContent.replaceAll(replacement.regexp, replacement.replacement);
					}
				}

				if (languageRaw) {

					respWriter.write(sectionContent);

				} else {

					String linkRel = wikitextProperties.getProperty(ObsoleteWebzConstants.WIKITEXT_LINK_REL_PROPERTY
							+ ObsoleteWebzConstants.SECTION_PROPERTY_SUFFIX + sectionName, defaultLinkRel);
					String absoluteLinkTarget = wikitextProperties.getProperty(
							ObsoleteWebzConstants.WIKITEXT_ABSOLUTE_LINK_TARGET_PROPERTY
									+ ObsoleteWebzConstants.SECTION_PROPERTY_SUFFIX + sectionName, defaultAbsoluteLinkTarget);

					HtmlDocumentBuilder builder = new HtmlDocumentBuilder(respWriter);
					// avoid the <html> and <body> tags
					builder.setEmitAsDocument(false);

					if (linkRel != null) {
						builder.setLinkRel(linkRel);
					}
					if (absoluteLinkTarget != null) {
						builder.setDefaultAbsoluteLinkTarget(absoluteLinkTarget);
					}

					MarkupParser parser = new MarkupParser(ServiceLocator.getInstance().getMarkupLanguage(language));
					parser.setBuilder(builder);
					parser.parse(sectionContent);

					// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
					// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
					// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
					// !!! =^_^= !!! =^_^= !!! =^_^= !!! WIKITEXT =^_^= INTEGRATED !!! =^_^= !!! =^_^= !!! =^_^= !!! \\
					// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
					// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\
					// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ \\

				}
			}
		}

		respWriter.write(templateString.substring(lastPos, templateString.length()));

		respWriter.flush();
	}

	private final class RegexpReplacement {
		private String regexp;
		private String replacement;
	}

	private Collection<RegexpReplacement> extractRegexpReplacements(Properties wikitextProperties,
			String wikitextPropertiesFolder, String sectionName) throws IOException, WebzException {
		String regexpsReferencePropertyName = ObsoleteWebzConstants.WIKITEXT_PRELIMINARY_REGEXPS_PROPERTY
				+ (sectionName == null ? ObsoleteWebzConstants.GENERAL_SECTION_SUFFIX
						: ObsoleteWebzConstants.SECTION_PROPERTY_SUFFIX + sectionName);
		String regexpPropertiesFile = wikitextProperties.getProperty(regexpsReferencePropertyName);

		if (regexpPropertiesFile == null) {
			return Collections.emptySet();
		} else {
			regexpPropertiesFile = wikitextPropertiesFolder + "/" + GenericWebzFile.trimFileSeparators(regexpPropertiesFile);

			Properties regexpProperties = new Properties();
			regexpProperties.load(new ByteArrayInputStream(fileFactory.get(regexpPropertiesFile).getFileContent(
					ObsoleteWebzConstants.DEFAULT_BUF_SIZE)));

			Map<String, RegexpReplacement> regexpReplacementsMap = new TreeMap<String, RegexpReplacement>();
			for (Map.Entry<Object, Object> entry : regexpProperties.entrySet()) {
				String property = entry.getKey().toString();

				if (property.startsWith(ObsoleteWebzConstants.REGEXP_PROPERTY_PREFIX)) {
					getReplacementObject(regexpReplacementsMap,
							property.substring(ObsoleteWebzConstants.REGEXP_PROPERTY_PREFIX.length())).regexp = entry
							.getValue().toString();
				} else if (property.startsWith(ObsoleteWebzConstants.REPLACEMENT_PROPERTY_PREFIX)) {
					getReplacementObject(regexpReplacementsMap,
							property.substring(ObsoleteWebzConstants.REPLACEMENT_PROPERTY_PREFIX.length())).replacement = entry
							.getValue().toString();
				} else {
					if (LOG.isTraceEnabled()) {
						LOG.trace("unrecognized property: " + property + " ( file: " + regexpPropertiesFile + " )");
					}
				}
			}

			Collection<RegexpReplacement> regexpReplacements = new ArrayList<RegexpReplacement>(regexpReplacementsMap.values()
					.size());
			for (Map.Entry<String, RegexpReplacement> entry : regexpReplacementsMap.entrySet()) {
				RegexpReplacement replacement = entry.getValue();

				if (replacement.regexp != null && replacement.replacement != null) {
					regexpReplacements.add(replacement);
				} else {
					if (LOG.isTraceEnabled()) {
						LOG.trace("replacement rule '" + entry.getKey() + "' is incomplete ( file: " + regexpPropertiesFile
								+ " )");
					}
				}
			}

			return regexpReplacements;
		}
	}

	private RegexpReplacement getReplacementObject(Map<String, RegexpReplacement> regexpReplacementsMap, String replacementName) {
		RegexpReplacement result = regexpReplacementsMap.get(replacementName);
		if (result == null) {
			result = new RegexpReplacement();
			regexpReplacementsMap.put(replacementName, result);
		}
		return result;
	}

	private String getSectionLanguage(Properties wikitextProperties, String sectionName, String defaultLanguage) {
		String language = wikitextProperties.getProperty(ObsoleteWebzConstants.WIKITEXT_LANG_PROPERTY
				+ ObsoleteWebzConstants.SECTION_PROPERTY_SUFFIX + sectionName, defaultLanguage);
		if (language != null) {
			language = language.trim();
		}
		return language;
	}

	private void getAllSectionsContent(Properties wikitextProperties, String contentString, Matcher contentSectionsMatcher,
			Map<String, String> sectionContentMap) {
		int lastPos = 0;
		String curSectionName = wikitextProperties.getProperty(ObsoleteWebzConstants.DEFAULT_SECTION_PROPERTY,
				ObsoleteWebzConstants.DEFAULT_SECTION_NAME);
		boolean sectionsTrim = Boolean.parseBoolean(wikitextProperties.getProperty(
				ObsoleteWebzConstants.SECTIONS_TRIM_PROPERTY, ObsoleteWebzConstants.DEFAULT_SECTION_TRIM.toString()));
		int sectionNameRegexpGroup = Integer.parseInt(wikitextProperties
				.getProperty(ObsoleteWebzConstants.SECTION_NAME_REGEXP_GROUP_PROPERTY));

		while (contentSectionsMatcher.find()) {

			getSectionContent(contentString, sectionContentMap, curSectionName, sectionsTrim, lastPos,
					contentSectionsMatcher.start());

			lastPos = contentSectionsMatcher.end();
			curSectionName = contentSectionsMatcher.group(sectionNameRegexpGroup);
		}

		getSectionContent(contentString, sectionContentMap, curSectionName, sectionsTrim, lastPos, contentString.length());
	}

	private void getSectionContent(String contentString, Map<String, String> sectionContentMap, String curSectionName,
			boolean sectionsTrim, int start, int end) {
		String sectionContent = contentString.substring(start, end);
		if (sectionsTrim) {
			sectionContent = sectionContent.trim();
		}

		sectionContentMap.put(curSectionName, sectionContent);
	}

	/**
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * Populates <code>resp</code> with 404 "Not Found" response if <code>populateNotFound</code> is <code>true</code>.
	 * Otherwise does nothing.
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 */
	private void populateNotFoundResponseIfNecessary(HttpServletRequest req, HttpServletResponse resp, boolean populateNotFound)
			throws WebzException, IOException {
		if (populateNotFound) {
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			populateResponse(ObsoleteWebzConstants.AUX_FILES_PREFIX + HttpServletResponse.SC_NOT_FOUND, req, resp, false, true);
		}
	}

	private String lookupInWikitexts(String pathName) {

		// TODO optimize

		pathName = pathName.toLowerCase();
		String propertyFile = null;
		for (Map.Entry<Object, Object> entry : wikitexts.entrySet()) {
			String property = entry.getKey().toString();
			if (property.startsWith(ObsoleteWebzConstants.DOT) && pathName.endsWith(property.toLowerCase())) {
				propertyFile = entry.getValue().toString();
				break;
			}
		}
		if (LOG.isTraceEnabled()) {
			LOG.trace("* wikitext property file:       " + propertyFile);
		}
		return propertyFile;
	}

	private String lookupMimetype(String pathName) {

		// TODO optimize

		pathName = pathName.toLowerCase();
		String mimetype = defaultMimetype;
		boolean contentTypeKnown = false;
		for (Map.Entry<Object, Object> entry : mimetypes.entrySet()) {
			String property = entry.getKey().toString();
			if (property.startsWith(ObsoleteWebzConstants.DOT) && pathName.endsWith(property.toLowerCase())) {
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

		domain = domain.trim().toLowerCase();
		String subfolder = null;
		for (Map.Entry<Object, Object> entry : domains.entrySet()) {
			if (domain.equals(entry.getKey().toString().toLowerCase().trim())) {
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
			return GenericWebzFile.trimFileSeparators(subfolder);
		}
	}

	private Pattern FILE_REGEXP = Pattern.compile("[\\\\/][^\\\\/]*$");

	private String trimToFolder(String path) {
		String[] pathParts = FILE_REGEXP.split(path);
		if (pathParts.length > 0) {
			return pathParts[0];
		} else {
			return "";
		}
	}

}
