package org.terems.webz.internals;

import java.util.Collection;

import org.terems.webz.WebzException;
import org.terems.webz.WebzFilter;

/** TODO !!! describe !!! **/
public interface WebzApp extends WebzServletContainerBridge {

	/** TODO !!! describe !!! **/
	public WebzApp init(WebzFileSystem fileSystem, Collection<Class<? extends WebzFilter>> filterClassesList,
			WebzDestroyableObjectFactory appFactory) throws WebzException;

	/** TODO !!! describe !!! **/
	public String getDisplayName();

}
