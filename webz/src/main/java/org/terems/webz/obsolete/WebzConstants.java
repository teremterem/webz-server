package org.terems.webz.obsolete;

import java.util.regex.Pattern;

import org.apache.commons.io.ByteOrderMark;

public class WebzConstants {

	public static final int DEFAULT_BUF_SIZE = 8192;
	public static final int DEFAULT_FILE_PAYLOAD_SIZE_THRESHOLD_TO_CACHE = 262144; // 16384;

	// ~

	public static final String AUX_FILES_PREFIX = "_";
	public static final String DOT = ".";

	public static final String _MIMETYPES_PROPERTIES_FILE = AUX_FILES_PREFIX + "mimetypes.properties";
	public static final String _DOMAINS_PROPERTIES_FILE = AUX_FILES_PREFIX + "domains.properties";
	public static final String _GENERAL_PROPERTIES_FILE = AUX_FILES_PREFIX + "general.properties";
	public static final String _WIKITEXTS_PROPERTIES_FILE = AUX_FILES_PREFIX + "wikitexts.properties";

	public static final String DEFAULT_FILE_SUFFIXES_PROPERTY = "default.file.suffixes.prioritized.cs.list";
	public static final String LAST_RESORT_WELCOME_FILE_PROPERTY = "last.resort.welcome.file";
	public static final String DEFAULT_MIMETYPE_PROPERTY = "default.mimetype";

	public static final String BASEAUTH_REALM_PROPERTY = "baseauth.realm";
	public static final String BASEAUTH_USERNAME_PROPERTY = "baseauth.username";
	public static final String BASEAUTH_PASSWORD_PROPERTY = "baseauth.password";

	// wikitext related properties:

	public static final String MIMETYPE_PROPERTY = "mimetype";
	public static final String TEMPLATE_PROPERTY = "template";

	public static final String OUTPUT_ENCODING_PROPERTY = "output.encoding";

	public static final String SECTION_VARS_REGEXP_PROPERTY = "section.vars.regexp";
	public static final String SECTION_NAME_REGEXP_GROUP_PROPERTY = "section.name.regexp.group";

	public static final String SECTIONS_TRIM_PROPERTY = "sections.trim";
	public static final String DEFAULT_SECTION_PROPERTY = "default.section";

	public static final String WIKITEXT_LANG_PROPERTY = "wikitext.language";
	public static final String WIKITEXT_LINK_REL_PROPERTY = "wikitext.link.rel";
	public static final String WIKITEXT_ABSOLUTE_LINK_TARGET_PROPERTY = "wikitext.absolute.link.target";
	public static final String WIKITEXT_PRELIMINARY_REGEXPS_PROPERTY = "wikitext.preliminary.regexps";
	public static final String WIKITEXT_REGEXPS_FOR_RAW_PROPERTY = "wikitext.regexp.replacements.for.raw";

	public static final String DEFAULT_PROPERTY_SUFFIX = ".default";
	public static final String GENERAL_SECTION_SUFFIX = ".general";
	public static final String SECTION_PROPERTY_SUFFIX = ".section."; // ***

	public static final String REGEXP_PROPERTY_PREFIX = "regexp.";
	public static final String REPLACEMENT_PROPERTY_PREFIX = "replacement.";

	// ~

	public static final String LANGUAGE_RAW = "raw";

	// ~

	public static final String HISTORY_VERSION_PREFIX = "v";
	public static final Pattern HISTORY_VERSION_REGEXP = Pattern.compile(HISTORY_VERSION_PREFIX + "(\\d+)");
	public static final int HISTORY_VERSION_NUMBER_REGEXP_GROUP = 1;

	public static final String WIKITEXT_INPUT_NAME = "wikitextInput";

	public static final String EDIT = "edit";
	public static final String PUBLISH = "publish";
	public static final String PREVIEW = "preview";
	public static final String SAVE_DRAFT = "saveDraft";
	public static final String IGNORE_DRAFT = "ignoreDraft";

	public static final String EDIT_PAGE_ENCODING_PROPERTY = "edit.page.encoding";

	public static final String EDIT_PAGE_TEMPLATE_PROPERTY = "edit.page.template";

	public static final String EDIT_INTERNAL_PATH_VAR_PROPERTY = "edit.internal.path.var";
	public static final String EDIT_TEXTAREA_CONTENT_VAR_PROPERTY = "edit.textarea.content.var";
	public static final String DRAFT_OPENED_VAR_PROPERTY = "draft.opened.var";
	public static final String DRAFT_IGNORED_VAR_PROPERTY = "draft.ignored.var";

	public static final String HISTORY_FOLDER_SUFFIX_PROPERTY = "history.folder.suffix";
	public static final String DRAFT_FILE_SUFFIX_PROPERTY = "draft.file.suffix";

	// the very defaults of the defaults:

	public static final String DEFAULT_ENCODING = ByteOrderMark.UTF_8.getCharsetName();

	public static final ByteOrderMark[] ALL_BOMS = { ByteOrderMark.UTF_8, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_16LE,
			ByteOrderMark.UTF_32BE, ByteOrderMark.UTF_32LE };

	public static final String DEFAULT_SECTION_NAME = "DEFAULT";
	public static final Boolean DEFAULT_SECTION_TRIM = Boolean.FALSE;

	public static final String DEFAULT_LAST_RESORT_WELCOME_FILE = "index.html";
	public static final String DEFAULT_MIMETYPE = "application/octet-stream";

	public static final Boolean DEFAULT_REGEXPS_FOR_RAW = Boolean.FALSE;

	public static final String DEFAULT_HISTORY_FOLDER_SUFFIX = ".history";
	public static final String DEFAULT_DRAFT_FILE_SUFFIX = ".draft";

}
