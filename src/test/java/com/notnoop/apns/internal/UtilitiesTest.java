package com.notnoop.apns.internal;

import org.junit.Assert;
import org.junit.Test;

public class UtilitiesTest {

	@Test
	public void testEncodeAndDecode() {
		String encodedHex = "a1b2d4";

		byte[] decoded = Utilities.decodeHex(encodedHex);
		String encoded = Utilities.encodeHex(decoded);

		Assert.assertEquals(encodedHex.toLowerCase(), encoded.toLowerCase());
	}
}
