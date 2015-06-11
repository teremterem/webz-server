/*
 * WebZ Server can serve web pages from various local and remote file sources.
 * Copyright (C) 2014-2015  Oleksandr Tereschenko <http://www.terems.org/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.terems.webz.impl;

import java.io.IOException;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.terems.webz.WebzException;
import org.terems.webz.WebzReaderDownloader;
import org.terems.webz.internals.WebzDeployer;
import org.terems.webz.internals.WebzFileSystem;
import org.terems.webz.util.WebzUtils;

public class WebzJavascriptDeployer implements WebzDeployer {

	public static final String WEBZ_DEPLOYER_JS_FILE = "webz-deployer.js";

	public static final String WEBZ_DEPLOY_JS_FUNCTION = "webzDeploy";

	private WebzFileSystem deploymentMetadata;

	public WebzJavascriptDeployer(WebzFileSystem deploymentMetadata) {
		this.deploymentMetadata = deploymentMetadata;
	}

	@Override
	public WebzServer deploy() throws WebzException {

		try {
			WebzReaderDownloader webzDeployerScript = deploymentMetadata.getFileFactory().get(WEBZ_DEPLOYER_JS_FILE).getFileDownloader();
			if (webzDeployerScript == null) {
				throw new WebzException("'" + WEBZ_DEPLOYER_JS_FILE + "' was not found in '" + deploymentMetadata.getUniqueId()
						+ "' file system");
			}

			ScriptEngine jsEngine = WebzUtils.createJavascriptEngine();
			jsEngine.eval(webzDeployerScript.getReader());

			WebzServer webzServer = new WebzServer();
			return (WebzServer) WebzUtils.assertInvocable(jsEngine).invokeFunction(WEBZ_DEPLOY_JS_FUNCTION, deploymentMetadata, webzServer);

		} catch (ScriptException | NoSuchMethodException | IOException e) {
			throw new WebzException(e);
		}
	}

}
