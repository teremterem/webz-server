package org.terems.webz;

import java.util.Collection;

import org.terems.webz.internals.WebzFileSystem;
import org.terems.webz.internals.WebzServletContainerBridge;
import org.terems.webz.plugin.WebzFilter;

/** TODO !!! describe !!! **/
public interface WebzApp extends WebzServletContainerBridge {

	/** TODO !!! describe !!! **/
	public void init(WebzFileSystem fileSystem, Collection<Class<? extends WebzFilter>> filterClassesList) throws WebzException;

	/** TODO !!! describe !!! **/
	public String getDisplayName();

}
