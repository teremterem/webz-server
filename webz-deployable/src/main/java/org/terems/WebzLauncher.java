package org.terems;

import java.io.File;

import org.apache.catalina.startup.Tomcat;

public class WebzLauncher {

	public static void main(String[] args) throws Exception {

		File webzWar = new File("webz.war");
		if (!webzWar.exists()) {

			File upperWebzWar = new File("../webz.war");
			if (upperWebzWar.exists()) {
				webzWar = upperWebzWar;
			}
		}

		Tomcat tomcat = new Tomcat();

		// The port that we should run on can be set into an environment variable
		// Look for that variable and default to 8887 if it isn't there.
		String webPort = System.getenv("PORT");
		if (webPort == null || webPort.isEmpty()) {
			webPort = "8887";
		}

		tomcat.setPort(Integer.valueOf(webPort));

		tomcat.addWebapp("/", webzWar.getAbsolutePath());
		System.out.println("Deploying WebZ from: " + webzWar);

		tomcat.start();
		tomcat.getServer().await();
	}

}