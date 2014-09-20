package org.terems.webz.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terems.webz.WebzApp;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFileSystem;
import org.terems.webz.WebzServletContainerBridge;
import org.terems.webz.cache.WebzFileSystemCache;
import org.terems.webz.impl.cache.CachedFileSystem;
import org.terems.webz.plugin.WebzFilter;
import org.terems.webz.settings.WebzDefaults;
import org.terems.webz.settings.WebzProperties;

public class WebzEngine implements WebzServletContainerBridge {

	private static final Logger LOG = LoggerFactory.getLogger(WebzEngine.class);

	private WebzDestroyableFactory globalFactory = new WebzDestroyableFactory();
	private WebzApp rootWebzApp;

	public WebzEngine(Properties rootFileSystemProperties, Collection<Class<? extends WebzFilter>> filterClassesList) throws WebzException {

		WebzFileSystem rootFileSystem = createFileSystem(rootFileSystemProperties);

		rootWebzApp = globalFactory.newDestroyable(GenericWebzApp.class);
		rootWebzApp.init(rootFileSystem, filterClassesList);

		LOG.info("WebZ Engine started\n");
	}

	@Override
	public void serve(HttpServletRequest req, HttpServletResponse resp) throws IOException, WebzException {

		if (rootWebzApp == null) {
			throw new WebzException("WebZ Engine is already stopped");
		}
		rootWebzApp.serve(req, resp);
	}

	@Override
	public void destroy() {

		rootWebzApp = null;
		LOG.info("WebZ Engine stopped\n");

		globalFactory.destroy();
	}

	private WebzFileSystem createFileSystem(Properties properties) throws WebzException {

		WebzFileSystem fileSystem = globalFactory.newDestroyable(properties.getProperty(WebzProperties.FS_IMPL_CLASS_PROPERTY,
				WebzDefaults.FS_IMPL_CLASS));
		fileSystem.init(properties);

		boolean cacheEnabled = Boolean.valueOf(properties.getProperty(WebzProperties.FS_CACHE_ENABLED_PROPERTY,
				String.valueOf(WebzDefaults.FS_CACHE_ENABLED)));
		if (cacheEnabled) {

			WebzFileSystemCache fsCache = globalFactory.newDestroyable(properties.getProperty(WebzProperties.FS_CACHE_IMPL_CLASS_PROPERTY,
					WebzDefaults.FS_CACHE_IMPL_CLASS));
			int payloadThreshold = Integer.valueOf(properties.getProperty(WebzProperties.FS_CACHE_PAYLOAD_THRESHOLD_BYTES_PROPERTY,
					String.valueOf(WebzDefaults.FS_CACHE_PAYLOAD_THRESHOLD_BYTES)));

			fileSystem = new CachedFileSystem(fileSystem, fsCache, payloadThreshold);
		}

		return fileSystem;
	}

}
