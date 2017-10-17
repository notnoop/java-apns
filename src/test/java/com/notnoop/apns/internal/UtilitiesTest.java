/*
 *  Copyright 2009, Mahmood Ali.
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 *
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following disclaimer
 *      in the documentation and/or other materials provided with the
 *      distribution.
 *    * Neither the name of Mahmood Ali. nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.notnoop.apns.internal;

import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProtocolException;

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

        // See http://en.wikipedia.org/wiki/Unicode_equivalence#Example
        //
        // Oh the joy as different platforms choose to normalize Unicode differently ... but both are valid.
        //
        // This is intended to fix a problem under jdk 6, I was not able to reproduce it with jdk 7u51 on OSX Mavericks
        // (Java seems to also use expected_NFC here).
        byte[] expected_NFC = {
                'e', 's', 'e', 'm', (byte)0x00C3, (byte)0x00A9, 'n', 'y'
        };

        byte[] expected_NFD = {
                'e', 's', 'e', 'm', (byte)0x00cc, (byte)0x0081, (byte)0x0061, 'n', 'y'
        };

        final byte[] bytes = Utilities.toUTF8Bytes(m);

        if (bytes.length == 8) {
            Assert.assertArrayEquals(expected_NFC, bytes);
        } else {
            Assert.assertArrayEquals(expected_NFD, bytes);
        }

    }

    @Test
    public void testHostOrIpLoopback() throws IOException {
        String hostname = Utilities.getHostOrIp(new InetSocketAddress("127.0.0.1", 8080));
        assertTrue("getHostOrIp", "127.0.0.1".equals(hostname));
    }

    @Test(expected = ProtocolException.class)
    public void testHostOrIpInvalidHost() throws IOException {
        Utilities.getHostOrIp(new InetSocketAddress("someveryveryveryv3ryveryv3ryveryveryv3rylonginvalidhostname", 8080));
    }

    @Test
    public void testHostOrIpValidHost() throws IOException {
        String hostname = Utilities.getHostOrIp(new InetSocketAddress("example.org", 8080));
        assertTrue("getHostOrIp", "example.org".equals(hostname));
    }
}
