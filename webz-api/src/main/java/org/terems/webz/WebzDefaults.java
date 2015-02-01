package org.terems.webz;

/** TODO !!! describe constants !!! **/
public class WebzDefaults {

	public static final boolean FS_CACHE_ENABLED = true;
	public static final boolean USE_METADATA_INFLATABLE_FILES = true;
	public static final String FS_IMPL_CLASS = "org.terems.webz.impl.LocalFileSystem";
	public static final String FS_CACHE_IMPL_CLASS = "org.terems.webz.impl.cache.ehcache.EhcacheFileSystemCache";
	public static final int FS_CACHE_PAYLOAD_THRESHOLD_BYTES = 262144;

	public static final String MIMETYPE = "application/octet-stream";
	public static final String ENCODING = "UTF-8"; // ByteOrderMark.UTF_8.getCharsetName();

	public static final String WELCOME_EXTENSIONS_LIST = ".html";
	public static final String USE_PARENT_FOLDER_NAME = "..";
	public static final String WELCOME_FILENAMES_LIST = "index," + USE_PARENT_FOLDER_NAME;
}
