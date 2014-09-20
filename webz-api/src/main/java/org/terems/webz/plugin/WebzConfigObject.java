package org.terems.webz.plugin;

import java.io.IOException;

import org.terems.webz.WebzDestroyable;
import org.terems.webz.WebzException;
import org.terems.webz.WebzFile;

/** TODO !!! describe !!! **/
public interface WebzConfigObject extends WebzDestroyable {

	/** TODO !!! describe !!! **/
	public void init(WebzFile configFolder) throws IOException, WebzException;

}
