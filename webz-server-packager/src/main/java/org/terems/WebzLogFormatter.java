package org.terems;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class WebzLogFormatter extends Formatter {

	@Override
	public String format(LogRecord record) {
		return appendNewlineIfNeeded(formatMessage(record));
	}

	protected String appendNewlineIfNeeded(String message) {

		// TODO TODODODODO TODO

		return message;
	}

}
