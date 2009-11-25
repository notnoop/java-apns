package com.notnoop.apns.internal;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.net.SocketFactory;

import org.junit.Assert;
import org.junit.Test;

import com.notnoop.apns.ApnsNotification;

import static org.mockito.Mockito.*;

public class ApnsConnectionTest {

	@Test
	public void testWithMockServer() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		SocketFactory sf = mockSocket(baos);

		ApnsConnection connection = new ApnsConnection(sf, "localhost", 80);

		String deviceToken = "a87d8878d878a79";
		String payload = "{\"aps\":{}}";
		ApnsNotification msg = new ApnsNotification (deviceToken, payload);

		connection.sendMessage(msg);

		Assert.assertArrayEquals(msg.marshall(), baos.toByteArray());
	}

	private SocketFactory mockSocket(OutputStream stream) {
		try {
			Socket socket = mock(Socket.class);
			when(socket.getOutputStream()).thenReturn(stream);

			SocketFactory factory = mock(SocketFactory.class);
			when(factory.createSocket()).thenReturn(socket);
			when(factory.createSocket(anyString(), anyInt())).thenReturn(socket);

			return factory;
		} catch (Exception e) {
			throw new AssertionError("Cannot be here!");
		}
	}
}
