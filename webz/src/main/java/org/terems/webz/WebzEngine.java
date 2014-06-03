package org.terems.webz;

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
import org.terems.webz.base.BaseWebzFile;

public class WebzEngine {

	private static Logger LOG = LoggerFactory.getLogger(WebzEngine.class);

	private WebzFileSystem fileSystem;

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

	public WebzEngine(WebzFileSystem fileSystem) throws WebzException {
		this.fileSystem = fileSystem;
		initMimetypes();
	}

	private void initMimetypes() throws WebzException {

		// TODO implement properties refresh mechanism (for ex. based on properties files update time)

		try {
			mimetypes.load(new ByteArrayInputStream(fileSystem.get(WebzConstants._MIMETYPES_PROPERTIES_FILE).getFileContent(
					WebzConstants.DEFAULT_BUF_SIZE)));
			domains.load(new ByteArrayInputStream(fileSystem.get(WebzConstants._DOMAINS_PROPERTIES_FILE).getFileContent(
					WebzConstants.DEFAULT_BUF_SIZE)));
			general.load(new ByteArrayInputStream(fileSystem.get(WebzConstants._GENERAL_PROPERTIES_FILE).getFileContent(
					WebzConstants.DEFAULT_BUF_SIZE)));
			wikitexts.load(new ByteArrayInputStream(fileSystem.get(WebzConstants._WIKITEXTS_PROPERTIES_FILE).getFileContent(
					WebzConstants.DEFAULT_BUF_SIZE)));

			baseauthRealm = general.getProperty(WebzConstants.BASEAUTH_REALM_PROPERTY, "");
			baseauthUsername = general.getProperty(WebzConstants.BASEAUTH_USERNAME_PROPERTY, "");
			baseauthPassword = general.getProperty(WebzConstants.BASEAUTH_PASSWORD_PROPERTY);

			defaultFileSuffixesPrioritized = general.getProperty(WebzConstants.DEFAULT_FILE_SUFFIXES_PROPERTY, ".html").split(
					"\\s*,\\s*");
			for (int i = 0; i < defaultFileSuffixesPrioritized.length; i++) {
				defaultFileSuffixesPrioritized[i] = defaultFileSuffixesPrioritized[i].trim().toLowerCase();
			}

			defaultMimetype = general.getProperty(WebzConstants.DEFAULT_MIMETYPE_PROPERTY, WebzConstants.DEFAULT_MIMETYPE);
			lastResortWelcomeFile = general.getProperty(WebzConstants.LAST_RESORT_WELCOME_FILE_PROPERTY,
					WebzConstants.DEFAULT_LAST_RESORT_WELCOME_FILE);

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
		return general.containsKey(WebzConstants.BASEAUTH_PASSWORD_PROPERTY);
	}

	private boolean proceedWithBaseAuthentication(HttpServletRequest req) {
		String authHeader = req.getHeader("Authorization");
		if (authHeader != null) {
			StringTokenizer st = new StringTokenizer(authHeader);
			if (st.hasMoreTokens()) {
				String basic = st.nextToken();

				if (basic.equalsIgnoreCase("Basic")) {
					try {
						String credentials = new String(Base64.decodeBase64(st.nextToken()), WebzConstants.DEFAULT_ENCODING);
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
			pathName = BaseWebzFile.trimFileSeparators(pathName);
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
			populateResponse(WebzConstants.AUX_FILES_PREFIX + statusCode, req, resp, false, true);
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
		WebzFile file = fileSystem.get(pathName);
		if (!file.exits()) {
			if (tryStandardSuffixes) {
				return tryWithOneOfStandardSuffixes(pathName, req, resp, populateNotFound);
			} else {
				populateNotFoundResponseIfNecessary(req, resp, populateNotFound);
				return false;
			}
		} else {
			if (file.isFile()) {
				return populateResponseFromFile(file, req, resp);
			} else {
				if (LOG.isTraceEnabled()) {
					LOG.trace("");
					LOG.trace("!!! Folder Name from Metadata: " + file.getName());
					LOG.trace("!!! Path Name: " + pathName);
				}

				String welcomePath = pathName;
				if (!(welcomePath.length() <= 0 || welcomePath.endsWith("/") || welcomePath.endsWith("\\"))) {
					welcomePath += '/';
				}

				boolean traditionalWelcome = populateResponse(welcomePath + lastResortWelcomeFile, req, resp, false,
						tryStandardSuffixes);
				if (!traditionalWelcome) {
					return populateResponse(welcomePath + file.getName(), req, resp, populateNotFound, tryStandardSuffixes);
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
			wikitextPropertiesFile = BaseWebzFile.trimFileSeparators(wikitextPropertiesFile);

			wikitextProperties = new Properties();
			wikitextProperties.load(new ByteArrayInputStream(fileSystem.get(wikitextPropertiesFile).getFileContent(
					WebzConstants.DEFAULT_BUF_SIZE)));

			mimetype = wikitextProperties.getProperty(WebzConstants.MIMETYPE_PROPERTY);
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
			if (req.getParameterMap().containsKey(WebzConstants.EDIT)) {

				editWikitext(file, resp, req.getParameterMap().containsKey(WebzConstants.IGNORE_DRAFT));

			} else if (req.getParameterMap().containsKey(WebzConstants.SAVE_DRAFT)) {

				saveWikitextDraft(file, req);
				resp.sendRedirect(req.getRequestURI() + "?" + WebzConstants.EDIT);

			} else if (req.getParameterMap().containsKey(WebzConstants.PUBLISH)) {

				publishWikitext(file, req);
				resp.sendRedirect(req.getRequestURI() + "?");

			} else if (req.getParameterMap().containsKey(WebzConstants.PREVIEW)) {

				viewWikitext(fileContentAsString(getNewWikitextContent(req)), resp, wikitextPropertiesFile, wikitextProperties);

			} else {

				viewWikitext(fileContentAsString(file.getFileContent()), resp, wikitextPropertiesFile, wikitextProperties);
			}
		}
		return true;
	}

	private byte[] getNewWikitextContent(HttpServletRequest req) throws WebzException, UnsupportedEncodingException {
		String wikitextNewContent = req.getParameter(WebzConstants.WIKITEXT_INPUT_NAME);
		if (wikitextNewContent == null) {
			throw new WebzException(WebzConstants.WIKITEXT_INPUT_NAME + " request parameter was not submitted");
		}

		String editPageEncoding = wikitexts.getProperty(WebzConstants.EDIT_PAGE_ENCODING_PROPERTY,
				WebzConstants.DEFAULT_ENCODING);
		return wikitextNewContent.getBytes(editPageEncoding);
	}

	private void editWikitext(WebzFile file, HttpServletResponse resp, boolean ignoreDraft) throws WebzException, IOException,
			UnsupportedEncodingException {

		String draftFilePathName = file.getPathName()
				+ wikitexts.getProperty(WebzConstants.DRAFT_FILE_SUFFIX_PROPERTY, WebzConstants.DEFAULT_DRAFT_FILE_SUFFIX);

		boolean showDraft = !ignoreDraft;
		if (showDraft) {
			WebzFile draftFile = fileSystem.get(draftFilePathName);

			showDraft = draftFile.exits();
			if (showDraft) {
				if (!draftFile.isFile()) {
					throw new WebzException("'" + draftFilePathName + "' is not a file");
				}

				file = draftFile;
			}
		}

		populateWikitextEditTemplate(resp, file, showDraft, ignoreDraft);
	}

	private void populateWikitextEditTemplate(HttpServletResponse resp, WebzFile file, boolean draftOpened, boolean draftIgnored)
			throws WebzException, IOException, UnsupportedEncodingException {
		String templateFile = wikitexts.getProperty(WebzConstants.EDIT_PAGE_TEMPLATE_PROPERTY);
		if (templateFile == null) {
			throw new WebzException(WebzConstants.EDIT_PAGE_TEMPLATE_PROPERTY + " property is not set in "
					+ WebzConstants._WIKITEXTS_PROPERTIES_FILE);
		}

		String templateString = fileContentAsString(fileSystem.get(templateFile).getFileContent());

		String contentString = fileContentAsString(file.getFileContent());

		String draftOpenedVar = wikitexts.getProperty(WebzConstants.DRAFT_OPENED_VAR_PROPERTY);
		String draftIgnoredVar = wikitexts.getProperty(WebzConstants.DRAFT_IGNORED_VAR_PROPERTY);
		String internalPathVar = wikitexts.getProperty(WebzConstants.EDIT_INTERNAL_PATH_VAR_PROPERTY);
		String textareaContentVar = wikitexts.getProperty(WebzConstants.EDIT_TEXTAREA_CONTENT_VAR_PROPERTY);

		templateString = templateString.replace(draftOpenedVar, String.valueOf(draftOpened));
		templateString = templateString.replace(draftIgnoredVar, String.valueOf(draftIgnored));
		templateString = templateString.replace(internalPathVar, "/" + file.getPathName());
		templateString = templateString.replace(textareaContentVar, StringEscapeUtils.escapeHtml4(contentString));

		String editPageEncoding = wikitexts.getProperty(WebzConstants.EDIT_PAGE_ENCODING_PROPERTY,
				WebzConstants.DEFAULT_ENCODING);
		writeStringToResp(resp, templateString, editPageEncoding);
	}

	private void writeStringToResp(HttpServletResponse resp, String stringToWrite, String outputEncoding)
			throws UnsupportedEncodingException, IOException {
		OutputStreamWriter respWriter = new OutputStreamWriter(resp.getOutputStream(), outputEncoding);
		respWriter.write(stringToWrite);
		respWriter.flush();
	}

	private WebzFile getDraftFile(WebzFile file) throws IOException, WebzException {
		return fileSystem.get(file.getPathName()
				+ wikitexts.getProperty(WebzConstants.DRAFT_FILE_SUFFIX_PROPERTY, WebzConstants.DEFAULT_DRAFT_FILE_SUFFIX));
	}

	private void saveWikitextDraft(WebzFile file, HttpServletRequest req) throws IOException, WebzException {
		getDraftFile(file).uploadFile(getNewWikitextContent(req));
	}

	private void publishWikitext(WebzFile file, HttpServletRequest req) throws WebzException, IOException,
			UnsupportedEncodingException {
		file.uploadFile(getNewWikitextContent(req));

		WebzFile draftFile = getDraftFile(file);
		if (draftFile.exits() && draftFile.isFile()) {
			draftFile.delete();
		}
	}

	private void viewWikitext(String contentString, HttpServletResponse resp, String wikitextPropertiesFile,
			Properties wikitextProperties) throws WebzException, IOException, UnsupportedEncodingException {
		String templateFile = wikitextProperties.getProperty(WebzConstants.TEMPLATE_PROPERTY);
		if (templateFile == null) {
			throw new WebzException(WebzConstants.TEMPLATE_PROPERTY + " property is not set in " + wikitextPropertiesFile);
		}

		String wikitextPropertiesFolder = trimToFolder(wikitextPropertiesFile);
		templateFile = wikitextPropertiesFolder + "/" + BaseWebzFile.trimFileSeparators(templateFile);

		String templateString = fileContentAsString(fileSystem.get(templateFile).getFileContent());

		String outputEncoding = wikitextProperties.getProperty(WebzConstants.OUTPUT_ENCODING_PROPERTY,
				WebzConstants.DEFAULT_ENCODING);
		processWikitext(resp, wikitextPropertiesFolder, wikitextProperties, templateString, contentString, outputEncoding);
	}

	private String fileContentAsString(byte[] fileContent) throws IOException, WebzException {
		StringWriter contentWriter = new StringWriter();

		BOMInputStream bomIn = new BOMInputStream(new ByteArrayInputStream(fileContent), false, WebzConstants.ALL_BOMS);

		String encoding = bomIn.getBOMCharsetName();
		if (encoding == null) {
			encoding = WebzConstants.DEFAULT_ENCODING;
		}

		IOUtils.copy(bomIn, contentWriter, encoding);
		String contentString = contentWriter.toString();

		bomIn.close();
		return contentString;
	}

	private void processWikitext(HttpServletResponse resp, String wikitextPropertiesFolder, Properties wikitextProperties,
			String templateString, String contentString, String outputEncoding) throws WebzException, IOException,
			UnsupportedEncodingException {
		Pattern sectionsRegexp = Pattern.compile(wikitextProperties.getProperty(WebzConstants.SECTION_VARS_REGEXP_PROPERTY));
		Matcher templateSectionsMatcher = sectionsRegexp.matcher(templateString);

		Matcher contentSectionsMatcher = sectionsRegexp.matcher(contentString);

		Map<String, String> sectionContentMap = new HashMap<String, String>();

		// ~

		getAllSectionsContent(wikitextProperties, contentString, contentSectionsMatcher, sectionContentMap);

		// ~

		Writer respWriter = new BufferedWriter(new OutputStreamWriter(resp.getOutputStream(), outputEncoding),
				WebzConstants.DEFAULT_BUF_SIZE);

		String defaultLinkRel = wikitextProperties.getProperty(WebzConstants.WIKITEXT_LINK_REL_PROPERTY
				+ WebzConstants.DEFAULT_PROPERTY_SUFFIX);
		String defaultAbsoluteLinkTarget = wikitextProperties.getProperty(WebzConstants.WIKITEXT_ABSOLUTE_LINK_TARGET_PROPERTY
				+ WebzConstants.DEFAULT_PROPERTY_SUFFIX);

		Collection<RegexpReplacement> generalRegexpReplacements = extractRegexpReplacements(wikitextProperties,
				wikitextPropertiesFolder, null);

		String defaultLanguage = wikitextProperties.getProperty(WebzConstants.WIKITEXT_LANG_PROPERTY
				+ WebzConstants.DEFAULT_PROPERTY_SUFFIX, WebzConstants.LANGUAGE_RAW);

		int sectionNameRegexpGroup = Integer.parseInt(wikitextProperties
				.getProperty(WebzConstants.SECTION_NAME_REGEXP_GROUP_PROPERTY));

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
				boolean languageRaw = WebzConstants.LANGUAGE_RAW.equals(language);

				boolean regexpsForRaw = Boolean.parseBoolean(wikitextProperties.getProperty(
						WebzConstants.WIKITEXT_REGEXPS_FOR_RAW_PROPERTY, WebzConstants.DEFAULT_REGEXPS_FOR_RAW.toString()));

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

					String linkRel = wikitextProperties.getProperty(WebzConstants.WIKITEXT_LINK_REL_PROPERTY
							+ WebzConstants.SECTION_PROPERTY_SUFFIX + sectionName, defaultLinkRel);
					String absoluteLinkTarget = wikitextProperties.getProperty(
							WebzConstants.WIKITEXT_ABSOLUTE_LINK_TARGET_PROPERTY + WebzConstants.SECTION_PROPERTY_SUFFIX
									+ sectionName, defaultAbsoluteLinkTarget);

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
		String regexpsReferencePropertyName = WebzConstants.WIKITEXT_PRELIMINARY_REGEXPS_PROPERTY
				+ (sectionName == null ? WebzConstants.GENERAL_SECTION_SUFFIX : WebzConstants.SECTION_PROPERTY_SUFFIX
						+ sectionName);
		String regexpPropertiesFile = wikitextProperties.getProperty(regexpsReferencePropertyName);

		if (regexpPropertiesFile == null) {
			return Collections.emptySet();
		} else {
			regexpPropertiesFile = wikitextPropertiesFolder + "/" + BaseWebzFile.trimFileSeparators(regexpPropertiesFile);

			Properties regexpProperties = new Properties();
			regexpProperties.load(new ByteArrayInputStream(fileSystem.get(regexpPropertiesFile).getFileContent(
					WebzConstants.DEFAULT_BUF_SIZE)));

			Map<String, RegexpReplacement> regexpReplacementsMap = new TreeMap<String, RegexpReplacement>();
			for (Map.Entry<Object, Object> entry : regexpProperties.entrySet()) {
				String property = entry.getKey().toString();

				if (property.startsWith(WebzConstants.REGEXP_PROPERTY_PREFIX)) {
					getReplacementObject(regexpReplacementsMap,
							property.substring(WebzConstants.REGEXP_PROPERTY_PREFIX.length())).regexp = entry.getValue()
							.toString();
				} else if (property.startsWith(WebzConstants.REPLACEMENT_PROPERTY_PREFIX)) {
					getReplacementObject(regexpReplacementsMap,
							property.substring(WebzConstants.REPLACEMENT_PROPERTY_PREFIX.length())).replacement = entry
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
		String language = wikitextProperties.getProperty(WebzConstants.WIKITEXT_LANG_PROPERTY
				+ WebzConstants.SECTION_PROPERTY_SUFFIX + sectionName, defaultLanguage);
		if (language != null) {
			language = language.trim();
		}
		return language;
	}

	private void getAllSectionsContent(Properties wikitextProperties, String contentString, Matcher contentSectionsMatcher,
			Map<String, String> sectionContentMap) {
		int lastPos = 0;
		String curSectionName = wikitextProperties.getProperty(WebzConstants.DEFAULT_SECTION_PROPERTY,
				WebzConstants.DEFAULT_SECTION_NAME);
		boolean sectionsTrim = Boolean.parseBoolean(wikitextProperties.getProperty(WebzConstants.SECTIONS_TRIM_PROPERTY,
				WebzConstants.DEFAULT_SECTION_TRIM.toString()));
		int sectionNameRegexpGroup = Integer.parseInt(wikitextProperties
				.getProperty(WebzConstants.SECTION_NAME_REGEXP_GROUP_PROPERTY));

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
			populateResponse(WebzConstants.AUX_FILES_PREFIX + HttpServletResponse.SC_NOT_FOUND, req, resp, false, true);
		}
	}

	private String lookupInWikitexts(String pathName) {

		// TODO optimize

		pathName = pathName.toLowerCase();
		String propertyFile = null;
		for (Map.Entry<Object, Object> entry : wikitexts.entrySet()) {
			String property = entry.getKey().toString();
			if (property.startsWith(WebzConstants.DOT) && pathName.endsWith(property.toLowerCase())) {
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
			if (property.startsWith(WebzConstants.DOT) && pathName.endsWith(property.toLowerCase())) {
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
			return BaseWebzFile.trimFileSeparators(subfolder);
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
