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
