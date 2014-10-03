package org.terems.webz.impl;

import java.util.Properties;

import org.terems.webz.WebzException;
import org.terems.webz.base.BaseWebzDestroyable;
import org.terems.webz.internals.WebzFileSystem;

public class WebzFileSystemManager extends BaseWebzDestroyable {

	private WebzDestroyableFactory factory;

	public static WebzFileSystemManager getManager(WebzDestroyableFactory factory) throws WebzException {

		WebzFileSystemManager manager = factory.getDestroyableSingleton(WebzFileSystemManager.class);
		manager.factory = factory;

		return manager;
	}

	public WebzFileSystem createFileSystem(Properties properties) throws WebzException {

		GenericWebzFileSystem fileSystem = factory.newDestroyable(GenericWebzFileSystem.class);
		fileSystem.factory = factory;

		fileSystem.init(properties, true);
		return fileSystem;
	}

}
