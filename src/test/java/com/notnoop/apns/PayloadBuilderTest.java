package com.notnoop.apns;

import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import com.notnoop.apns.PayloadBuilder;
import com.notnoop.apns.internal.Utilities;

public class PayloadBuilderTest {

    @Test
    public void testEmpty() {
        final PayloadBuilder builder = new PayloadBuilder();

        final String expected = "{\"aps\":{}}";
        final String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void testOneAps() {
        final PayloadBuilder builder = new PayloadBuilder();
        builder.alertBody("test");

        final String expected = "{\"aps\":{\"alert\":\"test\"}}";
        final String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void testTwoAps() {
        final PayloadBuilder builder = new PayloadBuilder();
        builder.alertBody("test");
        builder.badge(9);

        final String expected = "{\"aps\":{\"alert\":\"test\",\"badge\":9}}";
        final String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void testTwoApsMultipleBuilds() {
        final PayloadBuilder builder = new PayloadBuilder();
        builder.alertBody("test");
        builder.badge(9);

        final String expected = "{\"aps\":{\"alert\":\"test\",\"badge\":9}}";
        assertEqualsJson(expected, builder.build());
        assertEqualsJson(expected, builder.build());
    }

    @Test
    public void testIncludeBadge() {
        final String badge0 = APNS.newPayload().badge(0).toString();
        final String badgeNo = APNS.newPayload().clearBadge().toString();

        final String expected = "{\"aps\":{\"badge\":0}}";
        assertEqualsJson(expected, badge0);
        assertEqualsJson(expected, badgeNo);
    }

    @Test
    public void localizedOneWithArray() {
        final PayloadBuilder builder = new PayloadBuilder()
        .localizedKey("GAME_PLAY_REQUEST_FORMAT")
        .localizedArguments(new String[] { "Jenna", "Frank" });
        builder.sound("chime");

        final String expected = "{\"aps\":{\"sound\":\"chime\",\"alert\":{\"loc-key\":\"GAME_PLAY_REQUEST_FORMAT\",\"loc-args\":[\"Jenna\",\"Frank\"]}}}";
        final String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void localizedOneWithVarargs() {
        final PayloadBuilder builder = new PayloadBuilder()
        .localizedKey("GAME_PLAY_REQUEST_FORMAT")
        .localizedArguments("Jenna", "Frank");
        builder.sound("chime");

        final String expected = "{\"aps\":{\"sound\":\"chime\",\"alert\":{\"loc-key\":\"GAME_PLAY_REQUEST_FORMAT\",\"loc-args\":[\"Jenna\",\"Frank\"]}}}";
        final String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void localizedTwo() {
        final PayloadBuilder builder =
            new PayloadBuilder()
        .sound("chime")
        .localizedKey("GAME_PLAY_REQUEST_FORMAT")
        .localizedArguments(new String[] { "Jenna", "Frank" });

        final String expected = "{\"aps\":{\"sound\":\"chime\",\"alert\":{\"loc-key\":\"GAME_PLAY_REQUEST_FORMAT\",\"loc-args\":[\"Jenna\",\"Frank\"]}}}";
        final String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void customFieldSimple() {
        final PayloadBuilder builder = new PayloadBuilder();
        builder.alertBody("test");

        builder.customField("ache1", "what");
        builder.customField("ache2", 2);

        final String expected = "{\"ache1\":\"what\",\"ache2\":2,\"aps\":{\"alert\":\"test\"}}";
        final String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void customFieldArray() {
        final PayloadBuilder builder = new PayloadBuilder();
        builder.alertBody("test");

        builder.customField("ache1", Arrays.asList("a1", "a2"));
        builder.customField("ache2", new int[] { 1, 2 } );

        final String expected = "{\"ache1\":[\"a1\",\"a2\"],\"ache2\":[1,2],\"aps\":{\"alert\":\"test\"}}";
        final String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void customBody() {
        final PayloadBuilder builder = new PayloadBuilder();
        builder.alertBody("what").actionKey("Cancel");

        final String expected = "{\"aps\":{\"alert\":{\"action-loc-key\":\"Cancel\",\"body\":\"what\"}}}";
        final String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void multipleBuildCallsWithCustomBody() {
        final PayloadBuilder builder = new PayloadBuilder();
        builder.alertBody("what").actionKey("Cancel");

        final String expected = "{\"aps\":{\"alert\":{\"action-loc-key\":\"Cancel\",\"body\":\"what\"}}}";
        assertEqualsJson(expected, builder.build());
        assertEqualsJson(expected, builder.build());
    }

    @Test
    public void customBodyReverseOrder() {
        final PayloadBuilder builder = new PayloadBuilder();
        builder.actionKey("Cancel").alertBody("what");

        final String expected = "{\"aps\":{\"alert\":{\"action-loc-key\":\"Cancel\",\"body\":\"what\"}}}";
        final String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void alertNoView() {
        final PayloadBuilder builder = new PayloadBuilder();
        builder.actionKey(null).alertBody("what");

        final String expected = "{\"aps\":{\"alert\":{\"action-loc-key\":null,\"body\":\"what\"}}}";
        final String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void alertNoViewSimpler() {
        final PayloadBuilder builder = new PayloadBuilder();
        builder.noActionButton().alertBody("what");

        final String expected = "{\"aps\":{\"alert\":{\"action-loc-key\":null,\"body\":\"what\"}}}";
        final String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void alertWithImageOnly() {
        final PayloadBuilder builder = new PayloadBuilder();
        builder.launchImage("/test");

        final String expected = "{\"aps\":{\"alert\":{\"launch-image\":\"/test\"}}}";
        final String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void alertWithImageAndText() {
        final PayloadBuilder builder = new PayloadBuilder();
        builder.launchImage("/test").alertBody("hello");

        final String expected = "{\"aps\":{\"alert\":{\"launch-image\":\"/test\",\"body\":\"hello\"}}}";
        final String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void emptyApsWithFields() {
        final PayloadBuilder builder = new PayloadBuilder();
        builder.customField("achme2", new int[] { 5, 8 } );

        final String expected = "{\"achme2\":[5,8],\"aps\":{}}";
        final String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void abitComplicated() {
        final PayloadBuilder builder = new PayloadBuilder();
        builder.customField("achme", "foo");
        builder.sound("chime");
        builder.localizedKey("GAME_PLAY_REQUEST_FORMAT")
        .localizedArguments(new String[] { "Jenna", "Frank"});

        final String expected = "{\"achme\":\"foo\",\"aps\":{\"sound\":\"chime\",\"alert\":{\"loc-key\":\"GAME_PLAY_REQUEST_FORMAT\",\"loc-args\":[\"Jenna\",\"Frank\"]}}}";
        final String actual = builder.toString();
        assertEqualsJson(expected, actual);
    }

    @Test
    public void multipleBuildAbitComplicated() {
        final PayloadBuilder builder = new PayloadBuilder();
        builder.customField("achme", "foo");
        builder.sound("chime");
        builder.localizedKey("GAME_PLAY_REQUEST_FORMAT")
        .localizedArguments(new String[] { "Jenna", "Frank"});

        final String expected = "{\"achme\":\"foo\",\"aps\":{\"sound\":\"chime\",\"alert\":{\"loc-key\":\"GAME_PLAY_REQUEST_FORMAT\",\"loc-args\":[\"Jenna\",\"Frank\"]}}}";
        assertEqualsJson(expected, builder.build());
        assertEqualsJson(expected, builder.build());
    }

    @Test
    public void copyReturnsNewInstance() {
        final PayloadBuilder builder = new PayloadBuilder();
        builder.sound("chime");
        final PayloadBuilder copy = builder.copy();
        copy.badge(5);

        assertNotSame(builder, copy);

        final String expected = "{\"aps\":{\"sound\":\"chime\"}}";
        assertEqualsJson(expected, builder.build());

        final String copyExpected = "{\"aps\":{\"sound\":\"chime\",\"badge\":5}}";
        assertEqualsJson(copyExpected, copy.build());
    }

    @Test
    public void simpleEnglishLength() {
        final PayloadBuilder builder = new PayloadBuilder().alertBody("test");
        final String expected = "{\"aps\":{\"alert\":\"test\"}}";
        assertEqualsJson(expected, builder.build());
        final int actualLength = Utilities.toUTF8Bytes(expected).length;
        assertEquals(actualLength, builder.length());
        assertFalse(builder.isTooLong());
    }

    @Test
    public void abitComplicatedEnglishLength() {
        final byte[] dtBytes = new byte[32];
        new Random().nextBytes(dtBytes);

        final String deviceToken = Utilities.encodeHex(dtBytes);
        final PayloadBuilder builder = new PayloadBuilder().alertBody("test");

        final SimpleApnsNotification fromString = new SimpleApnsNotification(deviceToken, builder.build());
        final SimpleApnsNotification fromBytes = new SimpleApnsNotification(dtBytes, Utilities.toUTF8Bytes(builder.build()));

        final String expected = "{\"aps\":{\"alert\":\"test\"}}";
        final int actualPacketLength = 1 + 2 + dtBytes.length + 2 + /* payload length = */ Utilities.toUTF8Bytes(expected).length;
        assertEquals(actualPacketLength, fromString.length());
        assertEquals(actualPacketLength, fromBytes.length());
        assertEquals(expected.length(), fromString.getPayload().length);
        assertArrayEquals(fromString.marshall(), fromBytes.marshall());
        assertFalse(builder.isTooLong());
    }

    private String strOfLen(final int l) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < l; ++i) {
            sb.append('c');
        }
        final String alert = sb.toString();
        return alert;
    }

    private PayloadBuilder payloadOf(final int l) {
        return APNS.newPayload().alertBody(strOfLen(l));
    }

    @Test
    public void detectingLongMessages() {
        final String basic = "{\"aps\":{\"alert\":\"\"}}";
        final int wrapperOverhead = basic.length();
        final int cutoffForAlert = 256 - wrapperOverhead;

        final PayloadBuilder wayShort = payloadOf(1);
        assertFalse(wayShort.isTooLong());
        assertTrue(wayShort.length() == wrapperOverhead + 1);

        final PayloadBuilder bitShort = payloadOf(cutoffForAlert - 1);
        assertFalse(bitShort.isTooLong());
        assertTrue(bitShort.length() == wrapperOverhead + cutoffForAlert - 1);

        final PayloadBuilder border = payloadOf(cutoffForAlert);
        assertFalse(border.isTooLong());
        assertTrue(border.length() == wrapperOverhead + cutoffForAlert);
        assertTrue(border.length() == 256);

        final PayloadBuilder abitLong = payloadOf(cutoffForAlert + 1);
        assertTrue(abitLong.isTooLong());
        assertTrue(abitLong.length() == wrapperOverhead + cutoffForAlert + 1);

        final PayloadBuilder tooLong = payloadOf(cutoffForAlert + 1000);
        assertTrue(tooLong.isTooLong());
        assertTrue(tooLong.length() == wrapperOverhead + cutoffForAlert + 1000);
    }

    @Test
    public void shrinkLongMessages() {
        final String basic = "{\"aps\":{\"alert\":\"\"}}";
        final int wrapperOverhead = basic.length();
        final int cutoffForAlert = 256 - wrapperOverhead;
        final int max_length = 256;

        final PayloadBuilder wayShort = payloadOf(1);
        wayShort.shrinkBody();  // NOOP
        assertFalse(wayShort.isTooLong());
        assertTrue(wayShort.length() == wrapperOverhead + 1);

        final PayloadBuilder bitShort = payloadOf(cutoffForAlert - 1);
        bitShort.shrinkBody();  // NOOP
        assertFalse(bitShort.isTooLong());
        assertTrue(bitShort.length() == wrapperOverhead + cutoffForAlert - 1);

        final PayloadBuilder border = payloadOf(cutoffForAlert);
        assertFalse(border.isTooLong());    // NOOP
        assertTrue(border.length() == max_length);

        final PayloadBuilder abitLong = payloadOf(cutoffForAlert + 1);
        abitLong.shrinkBody();
        assertFalse(abitLong.isTooLong());
        assertTrue(abitLong.length() == max_length);

        final PayloadBuilder tooLong = payloadOf(cutoffForAlert + 1000);
        tooLong.shrinkBody();
        assertFalse(tooLong.isTooLong());
        assertTrue(tooLong.length() == max_length);
    }

    @Test
    public void shrinkLongMessagesWithOtherthigns() {
        final String basic = "{\"aps\":{\"alert\":\"\"}}";
        final int wrapperOverhead = basic.length();
        final int cutoffForAlert = 256 - wrapperOverhead;
        final int max_length = 256;

        final PayloadBuilder wayShort = payloadOf(1).sound("default");
        assertFalse(wayShort.isTooLong());
        assertTrue(wayShort.length() <= max_length);

        final PayloadBuilder bitShort = payloadOf(cutoffForAlert - 1).sound("default");
        bitShort.shrinkBody();  // NOOP
        assertFalse(bitShort.isTooLong());
        assertTrue(bitShort.length() <= max_length);

        final PayloadBuilder border = payloadOf(cutoffForAlert).sound("default");
        border.shrinkBody();
        assertFalse(border.isTooLong());    // NOOP
        assertTrue(border.length() == max_length);

        final PayloadBuilder abitLong = payloadOf(cutoffForAlert + 1).sound("default");
        abitLong.shrinkBody();
        assertFalse(abitLong.isTooLong());
        assertTrue(abitLong.length() == max_length);

        final PayloadBuilder tooLong = payloadOf(cutoffForAlert + 1000).sound("default");
        tooLong.shrinkBody();
        assertFalse(tooLong.isTooLong());
        assertTrue(tooLong.length() == max_length);
    }

    @Test
    public void removeAlertIfSooLong() {
        final PayloadBuilder tooLong =
            APNS.newPayload()
            .customField("test", strOfLen(256))
            .alertBody("what");
        tooLong.shrinkBody();
        final String payload = tooLong.build();
        assertThat(payload, not(containsString("alert")));

    }

    private void assertEqualsJson(final String expected, final String actual) {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            @SuppressWarnings("unchecked")
            final
            Map<String, Object> exNode = mapper.readValue(expected, Map.class),
                 acNode = mapper.readValue(actual, Map.class);
            assertEquals(exNode, acNode);
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    public void supportsMDM() {
        final String mdm = APNS.newPayload().mdm("213").toString();

        final String expected = "{\"mdm\":\"213\"}";
        assertEqualsJson(expected, mdm);
    }

    @Test
    public void supportsNewsstand() {
        final String news = APNS.newPayload().forNewsstand().toString();

        final String expected = "{\"aps\":{\"content-available\":1}}";
        assertEqualsJson(expected, news);
    }

    @Test
    public void tooLongWithCustomFields() {
        final PayloadBuilder builder = new PayloadBuilder();
        builder.alertBody("12345678");

        builder.customField("ache1", "what");
        builder.customField("ache2", 2);

        final String s1 = builder.toString();
        assertThat(s1, containsString("12345678"));
        final String s2 = builder.toString();
        assertThat(s2, containsString("12345678"));

        assertEqualsJson(s1, s2);
    }

    @Test
    public void trimWorksWithLongFields() {
        final PayloadBuilder builder = new PayloadBuilder();
        final String toolong =
                "1234567890123456789012345678901234567890" +
                "1234567890123456789012345678901234567890" +
                "1234567890123456789012345678901234567890" +
                "1234567890123456789012345678901234567890" +
                "1234567890123456789012345678901234567890" +
                "1234567890123456789012345678901234567890";

        builder.alertBody(toolong);
        builder.actionKey("OK");

        builder.shrinkBody();

        final String s2 = builder.toString();
        assertThat(s2, containsString("12345678"));
    }


    @Test
    public void utf8Encoding() {
        final String str = "esem�ny";

        final PayloadBuilder builder = new PayloadBuilder();
        final String s1 = builder.alertBody(str).toString();

        assertThat(s1, containsString(str));
    }

    @Test
    public void utf8EncodingEscaped() {
        final String str = "esem\u00E9ny";

        final PayloadBuilder builder = new PayloadBuilder();
        final String s1 = builder.alertBody(str).toString();

        assertThat(s1, containsString(str));
    }
}
