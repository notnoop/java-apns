package com.notnoop.apns;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class PayloadBuilderTest {

	@Test
	public void testEmpty() {
		PayloadBuilder builder = new PayloadBuilder();

		String expected = "{\"aps\":{}}";
		String actual = builder.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void testOneAps() {
		PayloadBuilder builder = new PayloadBuilder();
		builder.alert("test");

		String expected = "{\"aps\":{\"alert\":\"test\"}}";
		String actual = builder.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void testTwoAps() {
		PayloadBuilder builder = new PayloadBuilder();
		builder.alert("test");
		builder.badge(9);

		String expected = "{\"aps\":{\"alert\":\"test\",\"badge\":9}}";
		String actual = builder.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void localizedOne() {
		PayloadBuilder builder = new PayloadBuilder();
		builder.customAlert()
			.localizedKey("GAME_PLAY_REQUEST_FORMAT")
			.localizedArguments(new String[] { "Jenna", "Frank" });
		builder.sound("chime");

		String expected = "{\"aps\":{\"sound\":\"chime\",\"alert\":{\"loc-key\":\"GAME_PLAY_REQUEST_FORMAT\",\"loc-args\":[\"Jenna\",\"Frank\"]}}}";
		String actual = builder.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void localizedTwo() {
		PayloadBuilder builder = new PayloadBuilder();
		builder.sound("chime");
		builder.customAlert()
			.localizedKey("GAME_PLAY_REQUEST_FORMAT")
			.localizedArguments(new String[] { "Jenna", "Frank" });

		String expected = "{\"aps\":{\"sound\":\"chime\",\"alert\":{\"loc-key\":\"GAME_PLAY_REQUEST_FORMAT\",\"loc-args\":[\"Jenna\",\"Frank\"]}}}";
		String actual = builder.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void customFieldSimple() {
		PayloadBuilder builder = new PayloadBuilder();
		builder.alert("test");

		builder.customField("ache1", "what");
		builder.customField("ache2", 2);

		String expected = "{\"ache1\":\"what\",\"ache2\":2,\"aps\":{\"alert\":\"test\"}}";
		String actual = builder.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void customFieldArray() {
		PayloadBuilder builder = new PayloadBuilder();
		builder.alert("test");

		builder.customField("ache1", Arrays.asList("a1", "a2"));
		builder.customField("ache2", new int[] { 1, 2 } );

		String expected = "{\"ache1\":[\"a1\",\"a2\"],\"ache2\":[1,2],\"aps\":{\"alert\":\"test\"}}";
		String actual = builder.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void emptyApsWithFields() {
		PayloadBuilder builder = new PayloadBuilder();
		builder.customField("achme2", new int[] { 5, 8 } );

		String expected = "{\"achme2\":[5,8],\"aps\":{}}";
		String actual = builder.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void abitComplicated() {
		PayloadBuilder builder = new PayloadBuilder();
		builder.customField("achme", "foo");
		builder.sound("chime");
		builder.customAlert().localizedKey("GAME_PLAY_REQUEST_FORMAT")
			.localizedArguments(new String[] { "Jenna", "Frank"});

		String expected = "{\"achme\":\"foo\",\"aps\":{\"sound\":\"chime\",\"alert\":{\"loc-key\":\"GAME_PLAY_REQUEST_FORMAT\",\"loc-args\":[\"Jenna\",\"Frank\"]}}}";
		String actual = builder.toString();
		assertEquals(expected, actual);
	}
}
