package com.notnoop.apns.internal;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Random;

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
