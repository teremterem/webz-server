package org.terems.webz.impl;

import java.util.Properties;

import org.terems.webz.WebzException;
import org.terems.webz.WebzProperties;
import org.terems.webz.base.BaseWebzDestroyable;
import org.terems.webz.internals.WebzFileSystem;
import org.terems.webz.internals.WebzObjectFactory;

public class WebzFileSystemManager extends BaseWebzDestroyable {

	public static WebzFileSystemManager getManager(WebzObjectFactory factory) throws WebzException {

		WebzFileSystemManager manager = factory.getDestroyableSingleton(WebzFileSystemManager.class);
		manager.factory = factory;

		return manager;
	}

	private WebzObjectFactory factory;

	public WebzFileSystem createFileSystem(Properties properties) throws WebzException {
		return createFileSystem(properties == null ? null : new WebzProperties(properties));
	}

	public WebzFileSystem createFileSystem(WebzProperties webzProperties) throws WebzException {
		return factory.newDestroyable(GenericWebzFileSystem.class).init(webzProperties, factory);
	}

}
