/*
 * Copyright 2009, Mahmood Ali.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following disclaimer
 *     in the documentation and/or other materials provided with the
 *     distribution.
 *   * Neither the name of Mahmood Ali. nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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

    @Test
    public void testParsingBytes() {
        Assert.assertEquals(0xFF00FF00, Utilities.parseBytes(0xFF, 0, 0xFF, 0));
        Assert.assertEquals(0x00FF00FF, Utilities.parseBytes(0, 0xFF, 0, 0xFF));
        Assert.assertEquals(0xDEADBEEF, Utilities.parseBytes(0xDE, 0xAD, 0xBE, 0xEF));
        Assert.assertEquals(0x0EADBEEF, Utilities.parseBytes(0x0E, 0xAD, 0xBE, 0xEF));

        Assert.assertTrue(Utilities.parseBytes(0xF0, 0,0,0) < 0);
        Assert.assertTrue(Utilities.parseBytes(0x80, 0,0,0) < 0);
        Assert.assertTrue(Utilities.parseBytes(0x70, 0,0,0) > 0);
    }

    @Test
    public void testEncodingUTF8() {
        String m = "esemény";

        byte[] expected = {
                'e', 's', 'e', 'm', (byte)0x00C3, (byte)0x00A9, 'n', 'y'
        };

        Assert.assertArrayEquals(expected, Utilities.toUTF8Bytes(m));

    }
}
