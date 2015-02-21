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

package org.terems;

import java.io.File;

import org.apache.catalina.startup.Tomcat;

public class WebzLauncher {

	public static void main(String[] args) throws Exception {

		String appHome = System.getProperty("app.home");

		// TODO get rid of war name hardcode:
		String webzWarName = appHome + "/lib/webz-0.9.1-SNAPSHOT.war";
		File webzWar = new File(webzWarName);

		Tomcat tomcat = new Tomcat();
		tomcat.setBaseDir(appHome + "/temp");
		tomcat.setSilent(true);

		String port = System.getenv("PORT");
		if (port == null || port.isEmpty()) {
			port = "8887";
		}
		// TODO make WebZ Server log port number instead of Tomcat

		tomcat.setPort(Integer.valueOf(port));

		tomcat.addWebapp("/", webzWar.getAbsolutePath());
		System.out.println("Deploying WebZ from: " + webzWar);

		tomcat.start();
		tomcat.getServer().await();
	}

}