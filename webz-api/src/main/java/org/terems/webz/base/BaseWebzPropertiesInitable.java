/*
 * WebZ Server is a server that can serve web pages from various sources.
 * Copyright (C) 2013-2015  Oleksandr Tereschenko <http://www.terems.org/>
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

package org.terems.webz.base;

import java.util.Properties;

import org.terems.webz.WebzException;
import org.terems.webz.WebzProperties;
import org.terems.webz.WebzPropertiesInitable;

/** TODO !!! describe !!! **/
public abstract class BaseWebzPropertiesInitable extends BaseWebzDestroyable implements WebzPropertiesInitable {

	private WebzProperties properties;

	/** Do nothing by default... **/
	protected void init() throws WebzException {
	}

	/** TODO !!! describe !!! **/
	protected WebzProperties getProperties() {
		return properties;
	}

	/** TODO !!! describe !!! **/
	@Override
	public final void init(Properties properties) throws WebzException {

		if (properties == null) {
			throw new NullPointerException("null Properties");
		}
		init(new WebzProperties(properties));
	}

	/** TODO !!! describe !!! **/
	@Override
	public final void init(WebzProperties webzProperties) throws WebzException {

		if (webzProperties == null) {
			throw new NullPointerException("null WebzProperties");
		}
		this.properties = webzProperties;
		init();
	}

}
