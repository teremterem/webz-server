package org.terems.webz;

public class WebzConstants {

	public static final String AUX_FILES_PREFIX = "_";
	public static final String DOT = ".";

	public static final String EDIT = "edit";
	public static final String PREVIEW = "preview";
	public static final String SAVE = "save";

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

	public static final String TEMPLATE_ENCODING_PROPERTY = "template.encoding";
	public static final String CONTENT_ENCODING_PROPERTY = "content.encoding";

	public static final String SECTION_VARS_REGEXP_PROPERTY = "section.vars.regexp";
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

	public static final String EDIT_TEMPLATE_ENCODING_PROPERTY = "edit.template.encoding";
	public static final String EDIT_TEMPLATE_PROPERTY = "edit.template";
	public static final String EDIT_INTERNAL_PATH_VAR_PROPERTY = "edit.internal.path.var";
	public static final String EDIT_TEXTAREA_CONTENT_VAR_PROPERTY = "edit.textarea.content.var";

	// the very defaults of the defaults:

	public static final String DEFAULT_ENCODING = "utf8";

	public static final int SECTION_NAME_REGEXP_GROUP = 1;
	public static final String DEFAULT_SECTION_NAME = "DEFAULT";
	public static final Boolean DEFAULT_SECTION_TRIM = Boolean.TRUE;

	public static final String DEFAULT_LAST_RESORT_WELCOME_FILE = "index.html";
	public static final String DEFAULT_MIMETYPE = "application/octet-stream";

	public static final Boolean DEFAULT_REGEXPS_FOR_RAW = Boolean.FALSE;

}
