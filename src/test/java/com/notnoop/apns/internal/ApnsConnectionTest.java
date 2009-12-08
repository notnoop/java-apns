package com.notnoop.apns.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.net.SocketFactory;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.OngoingStubbing;

import com.notnoop.apns.ApnsNotification;

import static org.mockito.Mockito.*;

public class ApnsConnectionTest {
    ApnsNotification msg = new ApnsNotification ("a87d8878d878a79", "{\"aps\":{}}");

    ByteArrayOutputStream baos;
    SocketFactory sf;
    ApnsConnection connection;

    @Before public void setUp() {
        baos = new ByteArrayOutputStream();
    }

    @After public void after() {
        ApnsConnection connection = new ApnsConnection(sf, "localhost", 80);
        connection.sendMessage(msg);
        Assert.assertArrayEquals(msg.marshall(), baos.toByteArray());
    }

    @Test
	public void messageSentOnWire() {
		sf = mockSocket(baos);
	}

	@Test
	public void retryOnClosedSocket() {
		sf = mockClosedThenOpen(baos, true, 2);
	}

	@Test
	public void retryOnError() {
	    sf = mockClosedThenOpen(baos, false, 2);
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
			e.printStackTrace();
			throw new AssertionError("Cannot be here!");
		}
	}

	private SocketFactory mockClosedThenOpen(OutputStream stream, boolean isClosed, int tries) {
		try {
			List<Socket> socketMocks = new ArrayList<Socket>(tries + 1);

			for (int i = 1; i < tries; ++i) {
				Socket socket = mock(Socket.class);
				if (isClosed) {
				    when(socket.isClosed()).thenReturn(true);
				    when(socket.isConnected()).thenReturn(false);
	                when(socket.getOutputStream()).thenThrow(
	                        new AssertionError("Should have checked for closed connection"));
				} else {
				    when(socket.getOutputStream()).thenThrow(
				            new IOException("simulated IOException"));
				}
				socketMocks.add(socket);
			}

			Socket socket = mock(Socket.class);
			when(socket.getOutputStream()).thenReturn(stream);
			when(socket.isConnected()).thenReturn(true);
			socketMocks.add(socket);

			SocketFactory factory = mock(SocketFactory.class);
			OngoingStubbing<Socket> stubbing = when(factory.createSocket(anyString(), anyInt()));
			Socket first = socketMocks.remove(0);
			stubbing.thenReturn(first, socketMocks.toArray(new Socket[0]));

			return factory;
		} catch (Exception e) {
			e.printStackTrace();
			throw new AssertionError("Cannot be here!");
		}
	}
}
