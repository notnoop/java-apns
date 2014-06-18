package com.notnoop.apns.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.net.SocketFactory;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.OngoingStubbing;

import static org.mockito.Mockito.*;

public class MockingUtils {

    static SocketFactory mockSocketFactory(OutputStream out, InputStream in) {
        try {
            Socket socket = mock(Socket.class);
            when(socket.getOutputStream()).thenReturn(out);
            when(socket.getInputStream()).thenReturn(in);

            SocketFactory factory = mock(SocketFactory.class);
            when(factory.createSocket()).thenReturn(socket);
            when(factory.createSocket(anyString(), anyInt())).thenReturn(socket);

            return factory;
        } catch (Exception e) {
            e.printStackTrace();
            throw new AssertionError("Cannot be here!");
        }
    }

    static SocketFactory mockClosedThenOpenSocket(OutputStream out, InputStream in, boolean isClosed, int failedTries) {
        try {
            List<Socket> socketMocks = new ArrayList<Socket>(failedTries + 1);

            for (int i = 0; i < failedTries; ++i) {
                Socket socket = mock(Socket.class);
                if (isClosed) {
                    mockSocketClosed(socket);
                } else {
                    when(socket.getOutputStream()).thenThrow(
                            new IOException("simulated IOException"));
                    doAnswer(new DynamicMockSocketClosed(socket)).when(socket).close();
                }
                socketMocks.add(socket);
            }

            Socket socket = mock(Socket.class);
            when(socket.getOutputStream()).thenReturn(out);
            when(socket.getInputStream()).thenReturn(in);
            when(socket.isConnected()).thenReturn(true);
            socketMocks.add(socket);

            SocketFactory factory = mock(SocketFactory.class);
            OngoingStubbing<Socket> stubbing = when(factory.createSocket(anyString(), anyInt()));
            for (Socket t : socketMocks)
                stubbing = stubbing.thenReturn(t);

            return factory;
        } catch (Exception e) {
            e.printStackTrace();
            throw new AssertionError("Cannot be here!");
        }
    }

    private static void mockSocketClosed(final Socket socket) throws IOException {
        when(socket.isClosed()).thenReturn(true);
        when(socket.isConnected()).thenReturn(false);
        when(socket.getOutputStream()).thenThrow(
                new AssertionError("Should have checked for closed connection"));
    }

    /**
     * Change a mock socket's behaviour to be closed. Dynamically used from close()
     */
    private static class DynamicMockSocketClosed implements Answer<Void> {
        private final Socket socket;

        public DynamicMockSocketClosed(final Socket socket) {
            this.socket = socket;
        }

        @Override
        public Void answer(final InvocationOnMock invocation) throws Throwable {
            mockSocketClosed(socket);
            return null;
        }
    }
}
