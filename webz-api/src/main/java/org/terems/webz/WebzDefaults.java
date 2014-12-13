package org.terems.webz;

/** TODO !!! describe constants !!! **/
public class WebzDefaults {

	public static final boolean FS_CACHE_ENABLED = true;
	public static final boolean USE_METADATA_INFLATABLE_FILES = true;
	public static final String FS_IMPL_CLASS = "org.terems.webz.impl.LocalFileSystem";
	public static final String FS_CACHE_IMPL_CLASS = "org.terems.webz.impl.cache.ehcache.EhcacheFileSystemCache";
	public static final int FS_CACHE_PAYLOAD_THRESHOLD_BYTES = 262144;

	public static final String DEFAULT_MIMETYPE = "application/octet-stream";
	public static final String DEFAULT_ENCODING = "UTF-8"; // ByteOrderMark.UTF_8.getCharsetName();

}
