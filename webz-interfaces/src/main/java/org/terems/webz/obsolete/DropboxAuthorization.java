package org.terems.webz.obsolete;

//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.Locale;

//import javax.servlet.http.HttpServlet;

//import com.dropbox.core.DbxAppInfo;
//import com.dropbox.core.DbxAuthFinish;
//import com.dropbox.core.DbxException;
//import com.dropbox.core.DbxRequestConfig;
//import com.dropbox.core.DbxWebAuthNoRedirect;

//@SuppressWarnings("serial")
@Deprecated
public class DropboxAuthorization /*extends HttpServlet*/ {

	private static final String APP_KEY = "jq2afk0p8yb7l56";
	private static final String APP_SECRET = "pxtmr3ll9wj0v91";

	//private static final DbxRequestConfig DBX_CONFIG = new DbxRequestConfig("webz/0.1", Locale.getDefault().toString(),
	//		GaeHttpRequestor.INSTANCE/* for google app engine */);

	public static void main(String[] args) /*throws IOException, DbxException*/ {
/*
		DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);
		DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(DBX_CONFIG, appInfo);

		// Have the user sign in and authorize your app.
		String authorizeUrl = webAuth.start();
		System.out.println("1. Go to: " + authorizeUrl);
		System.out.println("2. Click \"Allow\" (you might have to log in first)");
		System.out.println("3. Copy the authorization code.");
		String code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();

		// This will fail if the user enters an invalid authorization code.
		DbxAuthFinish authFinish = webAuth.finish(code);

		System.out.println();
		System.out.println("  SUCCESS");
		System.out.println();
		System.out.println("********************************************************************************");
		System.out.println("*");
		System.out.println("*  accessToken: " + authFinish.accessToken);
		System.out.println("*");
		System.out.println("********************************************************************************");
		System.out.println("*");
		System.out.println("*  userId: " + authFinish.userId);
		System.out.println("*  urlState: " + authFinish.urlState);
		System.out.println("*");
		System.out.println("********************************************************************************");
		System.out.println();
		System.out.println();
*/
	}

}
