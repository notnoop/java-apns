package com.notnoop.apns.internal;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Random;

import org.junit.Test;
import static org.junit.matchers.JUnitMatchers.*;

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

    static int simpleDate = 0;
    static byte[] simple = pack(
            /* time_t */  new byte[] {0, 0, 0, 0},
            /* length */  new byte[] { 0, 32 },
            /* device token */ simpleDevice
            );

    static int firstDate = 10;
    static int secondDate = 1 << 8;
    static int thirdDate = secondDate;
    static byte[] three = pack(
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

    protected static void checkRawSimple(Map<Integer, byte[]> simpleParsed) {
        assertEquals(1, simpleParsed.size());
        assertThat(simpleParsed.keySet(), hasItem(simpleDate));
        assertArrayEquals(simpleDevice, simpleParsed.get(simpleDate));
    }

    protected static void checkRawThree(Map<Integer, byte[]> threeParsed) {
        assertEquals(3, threeParsed.size());
        Collection<Integer> times = threeParsed.keySet();
        assertThat(times, hasItems(firstDate, secondDate, thirdDate));

        assertArrayEquals(firstDevice, threeParsed.get(firstDate));
        assertArrayEquals(secondDevice, threeParsed.get(secondDate));
        assertArrayEquals(thirdDevice, threeParsed.get(firstDate));
    }

    protected static void checkParsedSimple(Map<String, Date> simpleParsed) {
        Date sd = new Date(simpleDate * 1000L);
        String deviceToken = Utilities.encodeHex(simpleDevice);

        assertEquals(1, simpleParsed.size());
        assertThat(simpleParsed.keySet(), hasItem(deviceToken));
        assertEquals(sd, simpleParsed.get(deviceToken));
    }

    protected static void checkParsedThree(Map<String, Date> threeParsed) {
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
