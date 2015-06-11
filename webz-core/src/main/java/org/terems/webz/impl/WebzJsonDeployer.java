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

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.terems.webz.WebzDefaults;
import org.terems.webz.WebzException;
import org.terems.webz.internals.WebzDeployer;
import org.terems.webz.internals.WebzDestroyableObjectFactory;
import org.terems.webz.internals.WebzFileSystem;
import org.terems.webz.internals.WebzNode;
import org.terems.webz.util.WebzUtils;

public class WebzJsonDeployer implements WebzDeployer {

	private WebzFileSystem jsonFolder;

	public WebzJsonDeployer(WebzFileSystem jsonFolder) {
		this.jsonFolder = jsonFolder;
	}

	@Override
	public WebzNode deploy() throws WebzException {

		try {
			ScriptEngine jsEngine = WebzUtils.createJavascriptEngine();
			jsEngine.eval(new InputStreamReader(getClass().getResourceAsStream("webz-json-deployer.js"), WebzDefaults.UTF8));

			WebzDestroyableObjectFactory objectFactory = new GenericWebzObjectFactory();
			return (WebzNode) WebzUtils.assertInvocable(jsEngine).invokeFunction("deployFromJson", jsonFolder, objectFactory);

		} catch (UnsupportedEncodingException | ScriptException | NoSuchMethodException e) {
			throw new WebzException(e);
		}
	}

}
