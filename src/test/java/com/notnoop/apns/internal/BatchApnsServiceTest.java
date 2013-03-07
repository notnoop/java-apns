package com.notnoop.apns.internal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;

import com.notnoop.apns.ApnsNotification;

public class BatchApnsServiceTest {

	private ApnsConnection prototype;
	private BatchApnsService service;

	private int delayTimeInSec = 2;
	private int delayTimeInSec_millis = delayTimeInSec * 1000; /* 2000 */
	private int delayTimeInSec1_2_millis = delayTimeInSec * 1000 / 2; /* 1000 */
	private int delayTimeInSec1_4_millis = delayTimeInSec * 1000 / 4; /* 500 */
	private int maxDelayTimeInSec = 2 * delayTimeInSec;

	@Before
	public void setup() {
		prototype = mock(ApnsConnection.class);
		when(prototype.copy()).thenReturn(prototype);

		service = new BatchApnsService(prototype, null, delayTimeInSec, maxDelayTimeInSec, Executors.defaultThreadFactory());
	}

	@Test
	public void simpleBatchWait_one() throws IOException, InterruptedException {
		// send message
		ApnsNotification message = service.push("1234", "{}");

		// make sure no message was send yet
		verify(prototype, times(0)).copy();
		verify(prototype, times(0)).sendMessage(message);
		verify(prototype, times(0)).close();

		Thread.sleep(delayTimeInSec_millis + /* for sure */250);

		// verify batch sends and close the connection
		verify(prototype, times(1)).copy();
		verify(prototype, times(1)).sendMessage(message);
		verify(prototype, times(1)).close();
	}

	@Test
	public void simpleBatchWait_multiple() throws IOException, InterruptedException {
		// send message
		ApnsNotification message1 = service.push("1234", "{}");
		Thread.sleep(delayTimeInSec1_2_millis);
		ApnsNotification message2 = service.push("4321", "{}");

		// make sure no message was send yet
		verify(prototype, times(0)).copy();
		verify(prototype, times(0)).sendMessage(message1);
		verify(prototype, times(0)).sendMessage(message2);
		verify(prototype, times(0)).close();

		Thread.sleep(delayTimeInSec1_4_millis * 3);

		// still no send
		verify(prototype, times(0)).copy();
		verify(prototype, times(0)).sendMessage(message1);
		verify(prototype, times(0)).sendMessage(message2);
		verify(prototype, times(0)).close();

		Thread.sleep(delayTimeInSec1_4_millis + /* for sure */250);

		// verify batch sends and close the connection
		verify(prototype, times(1)).copy();
		verify(prototype, times(1)).sendMessage(message1);
		verify(prototype, times(1)).sendMessage(message2);
		verify(prototype, times(1)).close();
	}

	@Test
	public void simpleBatchWait_maxDelay() throws IOException, InterruptedException {
		// send message
		ApnsNotification message1 = service.push("1234", "{}");
		Thread.sleep(delayTimeInSec1_4_millis * 3);
		ApnsNotification message2 = service.push("4321", "{}");
		Thread.sleep(delayTimeInSec1_4_millis * 3);
		ApnsNotification message3 = service.push("4321", "{}");
		Thread.sleep(delayTimeInSec1_4_millis * 3);
		ApnsNotification message4 = service.push("4321", "{}");

		// make sure no message was send yet
		verify(prototype, times(0)).copy();
		verify(prototype, times(0)).sendMessage(message1);
		verify(prototype, times(0)).sendMessage(message2);
		verify(prototype, times(0)).sendMessage(message3);
		verify(prototype, times(0)).sendMessage(message4);
		verify(prototype, times(0)).close();

		Thread.sleep(delayTimeInSec1_4_millis + /* for sure */250);

		// verify batch sends and close the connection
		verify(prototype, times(1)).copy();
		verify(prototype, times(1)).sendMessage(message1);
		verify(prototype, times(1)).sendMessage(message2);
		verify(prototype, times(1)).sendMessage(message3);
		verify(prototype, times(1)).sendMessage(message4);
		verify(prototype, times(1)).close();
	}

}
