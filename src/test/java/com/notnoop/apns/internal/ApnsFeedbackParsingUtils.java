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

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class ApnsFeedbackParsingUtils {
    static byte[] pack(byte[]... args) {
        int total = 0;
        for (byte[] arg : args)
            total += arg.length;

        byte[] result = new byte[total];

        int index = 0;
        for (byte[] arg : args) {
            System.arraycopy(arg, 0, result, index, arg.length);
            index += arg.length;
        }
        return result;
    }

    static byte[] simpleDevice = new byte[32];
    static byte[] firstDevice = new byte[32];
    static byte[] secondDevice = new byte[32];
    static byte[] thirdDevice = new byte[32];
    static {
        Random random = new Random();
        do {
            random.nextBytes(simpleDevice);
            random.nextBytes(firstDevice);
            random.nextBytes(secondDevice);
            random.nextBytes(thirdDevice);
        } while (Arrays.equals(firstDevice, secondDevice)
                   || (Arrays.equals(secondDevice, thirdDevice))
                   || (Arrays.equals(firstDevice, thirdDevice)));
    }

    static int simpleDate;
    public static byte[] simple = pack(
            /* time_t */  new byte[] {0, 0, 0, 0},
            /* length */  new byte[] { 0, 32 },
            /* device token */ simpleDevice
            );

    static int firstDate = 10;
    static int secondDate = 1 << 8;
    static int thirdDate = secondDate;
    public static byte[] three = pack(
            /* first message */
            /* time_t */  new byte[] {0, 0, 0, 10},
            /* length */  new byte[] { 0, 32 },
            /* device token */ firstDevice,

            /* second device */
            /* time_t */  new byte[] {0, 0, 1, 0},
            /* length */  new byte[] { 0, 32 },
            /* device token */ secondDevice,

            /* Duplicate time */
            /* time_t */  new byte[] {0, 0, 1, 0},
            /* length */  new byte[] { 0, 32 },
            /* device token */ thirdDevice
    );

    protected static void checkRawSimple(Map<byte[], Integer> simpleParsed) {
        assertEquals(1, simpleParsed.size());
        assertThat(simpleParsed.keySet(), hasItem(simpleDevice));

        for (Map.Entry<byte[], Integer> e : simpleParsed.entrySet()) {
            byte[] device = e.getKey();
            Integer date = e.getValue();
            if (Arrays.equals(simpleDevice, device)) {
                assertEquals(simpleDate, (int)date);
            } else {
                fail("Unexpected value in collection");
            }
        }
    }

    protected static void checkRawThree(Map<byte[], Integer> threeParsed) {
        assertEquals(3, threeParsed.size());
        Collection<byte[]> devices = threeParsed.keySet();
        assertThat(devices, hasItems(firstDevice, secondDevice, thirdDevice));

        for (Map.Entry<byte[], Integer> e : threeParsed.entrySet()) {
            byte[] device = e.getKey();
            Integer date = e.getValue();
            if (Arrays.equals(firstDevice, device)) {
                assertEquals(firstDate, (int)date);
            } else if (Arrays.equals(secondDevice, device)) {
                assertEquals(secondDate, (int)date);
            } else if (Arrays.equals(thirdDevice, device)) {
                assertEquals(thirdDate, (int)date);
            } else {
                fail("Unexpected value in collection");
            }
        }

    }

    public static void checkParsedSimple(Map<String, Date> simpleParsed) {
        Date sd = new Date(simpleDate * 1000L);
        String deviceToken = Utilities.encodeHex(simpleDevice);

        assertEquals(1, simpleParsed.size());
        assertThat(simpleParsed.keySet(), hasItem(deviceToken));
        assertEquals(sd, simpleParsed.get(deviceToken));
    }

    public static void checkParsedThree(Map<String, Date> threeParsed) {
        Date d1 = new Date(firstDate * 1000L);
        String dt1 = Utilities.encodeHex(firstDevice);

        Date d2 = new Date(secondDate * 1000L);
        String dt2 = Utilities.encodeHex(secondDevice);

        Date d3 = new Date(thirdDate * 1000L);
        String dt3 = Utilities.encodeHex(thirdDevice);

        assertEquals(3, threeParsed.size());
        assertThat(threeParsed.keySet(), hasItems(dt1, dt2, dt3));
        assertEquals(d1, threeParsed.get(dt1));
        assertEquals(d2, threeParsed.get(dt2));
        assertEquals(d3, threeParsed.get(dt3));
    }

}
