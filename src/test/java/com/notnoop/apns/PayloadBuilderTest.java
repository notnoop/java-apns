package com.notnoop.apns;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;

import static org.hamcrest.CoreMatchers.*;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import static org.junit.matchers.JUnitMatchers.*;

import com.notnoop.apns.internal.Utilities;

public class PayloadBuilderTest {

    @Test
    public void testEmpty() {
        PayloadBuilder builder = new PayloadBuilder();

        String expected = "{\"aps\":{}}";
        String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void testOneAps() {
        PayloadBuilder builder = new PayloadBuilder();
        builder.alertBody("test");

        String expected = "{\"aps\":{\"alert\":\"test\"}}";
        String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void testTwoAps() {
        PayloadBuilder builder = new PayloadBuilder();
        builder.alertBody("test");
        builder.badge(9);

        String expected = "{\"aps\":{\"alert\":\"test\",\"badge\":9}}";
        String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void testTwoApsMultipleBuilds() {
        PayloadBuilder builder = new PayloadBuilder();
        builder.alertBody("test");
        builder.badge(9);

        String expected = "{\"aps\":{\"alert\":\"test\",\"badge\":9}}";
        assertEqualsJson(expected, builder.build());
        assertEqualsJson(expected, builder.build());
    }

    @Test
    public void testIncludeBadge() {
        String badge0 = APNS.newPayload().badge(0).toString();
        String badgeNo = APNS.newPayload().clearBadge().toString();

        String expected = "{\"aps\":{\"badge\":0}}";
        assertEqualsJson(expected, badge0);
        assertEqualsJson(expected, badgeNo);
    }

    @Test
    public void localizedOneWithArray() {
        PayloadBuilder builder = new PayloadBuilder()
        .localizedKey("GAME_PLAY_REQUEST_FORMAT")
        .localizedArguments(new String[] { "Jenna", "Frank" });
        builder.sound("chime");

        String expected = "{\"aps\":{\"sound\":\"chime\",\"alert\":{\"loc-key\":\"GAME_PLAY_REQUEST_FORMAT\",\"loc-args\":[\"Jenna\",\"Frank\"]}}}";
        String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void localizedOneWithVarargs() {
        PayloadBuilder builder = new PayloadBuilder()
        .localizedKey("GAME_PLAY_REQUEST_FORMAT")
        .localizedArguments("Jenna", "Frank");
        builder.sound("chime");

        String expected = "{\"aps\":{\"sound\":\"chime\",\"alert\":{\"loc-key\":\"GAME_PLAY_REQUEST_FORMAT\",\"loc-args\":[\"Jenna\",\"Frank\"]}}}";
        String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void localizedTwo() {
        PayloadBuilder builder =
            new PayloadBuilder()
        .sound("chime")
        .localizedKey("GAME_PLAY_REQUEST_FORMAT")
        .localizedArguments(new String[] { "Jenna", "Frank" });

        String expected = "{\"aps\":{\"sound\":\"chime\",\"alert\":{\"loc-key\":\"GAME_PLAY_REQUEST_FORMAT\",\"loc-args\":[\"Jenna\",\"Frank\"]}}}";
        String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void customFieldSimple() {
        PayloadBuilder builder = new PayloadBuilder();
        builder.alertBody("test");

        builder.customField("ache1", "what");
        builder.customField("ache2", 2);

        String expected = "{\"ache1\":\"what\",\"ache2\":2,\"aps\":{\"alert\":\"test\"}}";
        String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void customFieldArray() {
        PayloadBuilder builder = new PayloadBuilder();
        builder.alertBody("test");

        builder.customField("ache1", Arrays.asList("a1", "a2"));
        builder.customField("ache2", new int[] { 1, 2 } );

        String expected = "{\"ache1\":[\"a1\",\"a2\"],\"ache2\":[1,2],\"aps\":{\"alert\":\"test\"}}";
        String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void customBody() {
        PayloadBuilder builder = new PayloadBuilder();
        builder.alertBody("what").actionKey("Cancel");

        String expected = "{\"aps\":{\"alert\":{\"action-loc-key\":\"Cancel\",\"body\":\"what\"}}}";
        String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void multipleBuildCallsWithCustomBody() {
        PayloadBuilder builder = new PayloadBuilder();
        builder.alertBody("what").actionKey("Cancel");

        String expected = "{\"aps\":{\"alert\":{\"action-loc-key\":\"Cancel\",\"body\":\"what\"}}}";
        assertEqualsJson(expected, builder.build());
        assertEqualsJson(expected, builder.build());
    }

    @Test
    public void customBodyReverseOrder() {
        PayloadBuilder builder = new PayloadBuilder();
        builder.actionKey("Cancel").alertBody("what");

        String expected = "{\"aps\":{\"alert\":{\"action-loc-key\":\"Cancel\",\"body\":\"what\"}}}";
        String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void alertNoView() {
        PayloadBuilder builder = new PayloadBuilder();
        builder.actionKey(null).alertBody("what");

        String expected = "{\"aps\":{\"alert\":{\"action-loc-key\":null,\"body\":\"what\"}}}";
        String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void alertNoViewSimpler() {
        PayloadBuilder builder = new PayloadBuilder();
        builder.noActionButton().alertBody("what");

        String expected = "{\"aps\":{\"alert\":{\"action-loc-key\":null,\"body\":\"what\"}}}";
        String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void alertWithImageOnly() {
        PayloadBuilder builder = new PayloadBuilder();
        builder.launchImage("/test");

        String expected = "{\"aps\":{\"alert\":{\"launch-image\":\"/test\"}}}";
        String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void alertWithImageAndText() {
        PayloadBuilder builder = new PayloadBuilder();
        builder.launchImage("/test").alertBody("hello");

        String expected = "{\"aps\":{\"alert\":{\"launch-image\":\"/test\",\"body\":\"hello\"}}}";
        String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void emptyApsWithFields() {
        PayloadBuilder builder = new PayloadBuilder();
        builder.customField("achme2", new int[] { 5, 8 } );

        String expected = "{\"achme2\":[5,8],\"aps\":{}}";
        String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void abitComplicated() {
        PayloadBuilder builder = new PayloadBuilder();
        builder.customField("achme", "foo");
        builder.sound("chime");
        builder.localizedKey("GAME_PLAY_REQUEST_FORMAT")
        .localizedArguments(new String[] { "Jenna", "Frank"});

        String expected = "{\"achme\":\"foo\",\"aps\":{\"sound\":\"chime\",\"alert\":{\"loc-key\":\"GAME_PLAY_REQUEST_FORMAT\",\"loc-args\":[\"Jenna\",\"Frank\"]}}}";
        String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void multipleBuildAbitComplicated() {
        PayloadBuilder builder = new PayloadBuilder();
        builder.customField("achme", "foo");
        builder.sound("chime");
        builder.localizedKey("GAME_PLAY_REQUEST_FORMAT")
        .localizedArguments(new String[] { "Jenna", "Frank"});

        String expected = "{\"achme\":\"foo\",\"aps\":{\"sound\":\"chime\",\"alert\":{\"loc-key\":\"GAME_PLAY_REQUEST_FORMAT\",\"loc-args\":[\"Jenna\",\"Frank\"]}}}";
        assertEqualsJson(expected, builder.build());
        assertEqualsJson(expected, builder.build());
    }

    @Test
    public void copyReturnsNewInstance() {
        PayloadBuilder builder = new PayloadBuilder();
        builder.sound("chime");
        PayloadBuilder copy = builder.copy();
        copy.badge(5);

        assertNotSame(builder, copy);

        String expected = "{\"aps\":{\"sound\":\"chime\"}}";
        assertEqualsJson(expected, builder.build());

        String copyExpected = "{\"aps\":{\"sound\":\"chime\",\"badge\":5}}";
        assertEqualsJson(copyExpected, copy.build());
    }

    @Test
    public void simpleEnglishLength() {
        PayloadBuilder builder = new PayloadBuilder().alertBody("test");
        String expected = "{\"aps\":{\"alert\":\"test\"}}";
        assertEqualsJson(expected, builder.build());
        int actualLength = Utilities.toUTF8Bytes(expected).length;
        assertEquals(actualLength, builder.length());
        assertFalse(builder.isTooLong());
    }

    @Test
    public void abitComplicatedEnglishLength() {
        byte[] dtBytes = new byte[32];
        new Random().nextBytes(dtBytes);

        String deviceToken = Utilities.encodeHex(dtBytes);
        PayloadBuilder builder = new PayloadBuilder().alertBody("test");

        SimpleApnsNotification fromString = new SimpleApnsNotification(deviceToken, builder.build());
        SimpleApnsNotification fromBytes = new SimpleApnsNotification(dtBytes, Utilities.toUTF8Bytes(builder.build()));

        String expected = "{\"aps\":{\"alert\":\"test\"}}";
        int actualPacketLength = 1 + 2 + dtBytes.length + 2 + /* payload length = */ Utilities.toUTF8Bytes(expected).length;
        assertEquals(actualPacketLength, fromString.length());
        assertEquals(actualPacketLength, fromBytes.length());
        assertEquals(expected.length(), fromString.getPayload().length);
        assertArrayEquals(fromString.marshall(), fromBytes.marshall());
        assertFalse(builder.isTooLong());
    }

    private String strOfLen(int l) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < l; ++i) {
            sb.append('c');
        }
        String alert = sb.toString();
        return alert;
    }

    private PayloadBuilder payloadOf(int l) {
        return APNS.newPayload().alertBody(strOfLen(l));
    }

    @Test
    public void detectingLongMessages() {
        String basic = "{\"aps\":{\"alert\":\"\"}}";
        int wrapperOverhead = basic.length();
        int cutoffForAlert = 256 - wrapperOverhead;

        PayloadBuilder wayShort = payloadOf(1);
        assertFalse(wayShort.isTooLong());
        assertTrue(wayShort.length() == wrapperOverhead + 1);

        PayloadBuilder bitShort = payloadOf(cutoffForAlert - 1);
        assertFalse(bitShort.isTooLong());
        assertTrue(bitShort.length() == wrapperOverhead + cutoffForAlert - 1);

        PayloadBuilder border = payloadOf(cutoffForAlert);
        assertFalse(border.isTooLong());
        assertTrue(border.length() == wrapperOverhead + cutoffForAlert);
        assertTrue(border.length() == 256);

        PayloadBuilder abitLong = payloadOf(cutoffForAlert + 1);
        assertTrue(abitLong.isTooLong());
        assertTrue(abitLong.length() == wrapperOverhead + cutoffForAlert + 1);

        PayloadBuilder tooLong = payloadOf(cutoffForAlert + 1000);
        assertTrue(tooLong.isTooLong());
        assertTrue(tooLong.length() == wrapperOverhead + cutoffForAlert + 1000);
    }

    @Test
    public void shrinkLongMessages() {
        String basic = "{\"aps\":{\"alert\":\"\"}}";
        int wrapperOverhead = basic.length();
        int cutoffForAlert = 256 - wrapperOverhead;
        int max_length = 256;

        PayloadBuilder wayShort = payloadOf(1);
        wayShort.shrinkBody();  // NOOP
        assertFalse(wayShort.isTooLong());
        assertTrue(wayShort.length() == wrapperOverhead + 1);

        PayloadBuilder bitShort = payloadOf(cutoffForAlert - 1);
        bitShort.shrinkBody();  // NOOP
        assertFalse(bitShort.isTooLong());
        assertTrue(bitShort.length() == wrapperOverhead + cutoffForAlert - 1);

        PayloadBuilder border = payloadOf(cutoffForAlert);
        assertFalse(border.isTooLong());    // NOOP
        assertTrue(border.length() == max_length);

        PayloadBuilder abitLong = payloadOf(cutoffForAlert + 1);
        abitLong.shrinkBody();
        assertFalse(abitLong.isTooLong());
        assertTrue(abitLong.length() == max_length);

        PayloadBuilder tooLong = payloadOf(cutoffForAlert + 1000);
        tooLong.shrinkBody();
        assertFalse(tooLong.isTooLong());
        assertTrue(tooLong.length() == max_length);
    }

    @Test
    public void shrinkLongMessagesWithOtherthigns() {
        String basic = "{\"aps\":{\"alert\":\"\"}}";
        int wrapperOverhead = basic.length();
        int cutoffForAlert = 256 - wrapperOverhead;
        int max_length = 256;

        PayloadBuilder wayShort = payloadOf(1).sound("default");
        assertFalse(wayShort.isTooLong());
        assertTrue(wayShort.length() <= max_length);

        PayloadBuilder bitShort = payloadOf(cutoffForAlert - 1).sound("default");
        bitShort.shrinkBody();  // NOOP
        assertFalse(bitShort.isTooLong());
        assertTrue(bitShort.length() <= max_length);

        PayloadBuilder border = payloadOf(cutoffForAlert).sound("default");
        border.shrinkBody();
        assertFalse(border.isTooLong());    // NOOP
        assertTrue(border.length() == max_length);

        PayloadBuilder abitLong = payloadOf(cutoffForAlert + 1).sound("default");
        abitLong.shrinkBody();
        assertFalse(abitLong.isTooLong());
        assertTrue(abitLong.length() == max_length);

        PayloadBuilder tooLong = payloadOf(cutoffForAlert + 1000).sound("default");
        tooLong.shrinkBody();
        assertFalse(tooLong.isTooLong());
        assertTrue(tooLong.length() == max_length);
    }

    @Test
    public void removeAlertIfSooLong() {
        PayloadBuilder tooLong =
            APNS.newPayload()
            .customField("test", strOfLen(256))
            .alertBody("what");
        tooLong.shrinkBody();
        String payload = tooLong.build();
        assertThat(payload, not(containsString("alert")));

    }

    private void assertEqualsJson(String expected, String actual) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> exNode = mapper.readValue(expected, Map.class),
                 acNode = mapper.readValue(actual, Map.class);
            assertEquals(exNode, acNode);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
