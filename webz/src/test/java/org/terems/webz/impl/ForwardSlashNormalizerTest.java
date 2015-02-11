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

package org.terems.webz.impl;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.terems.webz.WebzProperties;
import org.testng.annotations.Test;

public class ForwardSlashNormalizerTest {

	@Test
	public void testIsNormalizedPathnameInvalid() {

		ForwardSlashNormalizer normalizer = new ForwardSlashNormalizer();

		assertTrue(normalizer.isNormalizedPathnameInvalid("."));
		assertTrue(normalizer.isNormalizedPathnameInvalid(".."));

		assertTrue(normalizer.isNormalizedPathnameInvalid("./path"));
		assertTrue(normalizer.isNormalizedPathnameInvalid("../path"));

		assertTrue(normalizer.isNormalizedPathnameInvalid("some/."));
		assertTrue(normalizer.isNormalizedPathnameInvalid("some/.."));

		assertTrue(normalizer.isNormalizedPathnameInvalid("some/./path"));
		assertTrue(normalizer.isNormalizedPathnameInvalid("some/../path"));

		assertTrue(normalizer.isNormalizedPathnameInvalid("some//path"));
		assertTrue(normalizer.isNormalizedPathnameInvalid("some////path"));
		assertTrue(normalizer.isNormalizedPathnameInvalid("some////////path"));
		assertTrue(normalizer.isNormalizedPathnameInvalid("/some/path"));
		assertTrue(normalizer.isNormalizedPathnameInvalid("///some/path"));
		assertTrue(normalizer.isNormalizedPathnameInvalid("some/path/"));
		assertTrue(normalizer.isNormalizedPathnameInvalid("some/path///"));
		assertTrue(normalizer.isNormalizedPathnameInvalid("/some/path/"));

		assertTrue(normalizer.isNormalizedPathnameInvalid("//very///~/broken/../////./path/"));

		// "~" is safe because no shell is involved when file system is accessed through java.io.File
		assertFalse(normalizer.isNormalizedPathnameInvalid("~"));
		assertFalse(normalizer.isNormalizedPathnameInvalid("~~"));
		assertFalse(normalizer.isNormalizedPathnameInvalid("~."));
		assertFalse(normalizer.isNormalizedPathnameInvalid(".~"));
		assertFalse(normalizer.isNormalizedPathnameInvalid("..."));
		assertFalse(normalizer.isNormalizedPathnameInvalid("....."));

		assertFalse(normalizer.isNormalizedPathnameInvalid("~/path"));
		assertFalse(normalizer.isNormalizedPathnameInvalid("~~/path"));
		assertFalse(normalizer.isNormalizedPathnameInvalid("~./path"));
		assertFalse(normalizer.isNormalizedPathnameInvalid(".~/path"));
		assertFalse(normalizer.isNormalizedPathnameInvalid(".../path"));
		assertFalse(normalizer.isNormalizedPathnameInvalid("...../path"));

		assertFalse(normalizer.isNormalizedPathnameInvalid("some/~"));
		assertFalse(normalizer.isNormalizedPathnameInvalid("some/~~"));
		assertFalse(normalizer.isNormalizedPathnameInvalid("some/~."));
		assertFalse(normalizer.isNormalizedPathnameInvalid("some/.~"));
		assertFalse(normalizer.isNormalizedPathnameInvalid("some/..."));
		assertFalse(normalizer.isNormalizedPathnameInvalid("some/....."));

		assertFalse(normalizer.isNormalizedPathnameInvalid("some/path"));
		assertFalse(normalizer.isNormalizedPathnameInvalid("some/~/path"));
		assertFalse(normalizer.isNormalizedPathnameInvalid("some/~~/path"));
		assertFalse(normalizer.isNormalizedPathnameInvalid("some/~./path"));
		assertFalse(normalizer.isNormalizedPathnameInvalid("some/.~/path"));
		assertFalse(normalizer.isNormalizedPathnameInvalid("some/.../path"));
		assertFalse(normalizer.isNormalizedPathnameInvalid("some/...../path"));
	}

	@Test
	public void testIsHidden() {

		ForwardSlashNormalizer normalizer = new ForwardSlashNormalizer();

		assertTrue(normalizer.isHidden(WebzProperties.WEBZ_CONFIG_FOLDER));
		assertTrue(normalizer.isHidden("some/" + WebzProperties.WEBZ_CONFIG_FOLDER));
		assertTrue(normalizer.isHidden(WebzProperties.WEBZ_CONFIG_FOLDER + "/path"));
		assertTrue(normalizer.isHidden(WebzProperties.WEBZ_CONFIG_FOLDER + "/pat.h"));
		assertTrue(normalizer.isHidden("some/" + WebzProperties.WEBZ_CONFIG_FOLDER + "/path"));
		assertTrue(normalizer.isHidden("some/" + WebzProperties.WEBZ_CONFIG_FOLDER + "/pa.th"));
		assertTrue(normalizer.isHidden("..."));
		assertTrue(normalizer.isHidden(".git"));
		assertTrue(normalizer.isHidden(".project"));
		assertTrue(normalizer.isHidden("some/.project"));
		assertTrue(normalizer.isHidden(".project/path"));
		assertTrue(normalizer.isHidden(".project/pat.h"));
		assertTrue(normalizer.isHidden("some/.project/path"));

		assertFalse(normalizer.isHidden("path"));
		assertFalse(normalizer.isHidden("some/path"));
		assertFalse(normalizer.isHidden("p.ath"));
		assertFalse(normalizer.isHidden("s.ome/path"));
		assertFalse(normalizer.isHidden("some./path"));
		assertFalse(normalizer.isHidden("some/path."));
		assertFalse(normalizer.isHidden("s.ome/path."));
	}

}
