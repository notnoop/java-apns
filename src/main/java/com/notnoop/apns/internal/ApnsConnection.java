package com.notnoop.apns.internal;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

public class ApnsConnection {

    private final SocketFactory factory;
    private final String host;
    private final int port;

    public ApnsConnection(SocketFactory factory, String host, int port) {
        this.factory = factory;
        this.host = host;
        this.port = port;
    }

    protected void close() throws IOException {
        this.socket.close();
    }

    private Socket socket;
    private Socket socket() {
        if (socket == null || !socket.isConnected()) {
            try {
                socket = factory.createSocket(host, port);
            } catch (UnknownHostException e) {
            } catch (IOException e) {
            }
        }
        return socket;
    }

    private static final int RETRIES = 3;
    protected synchronized void sendMessage(ApnsMessage m) {
        int attempts = 0;
        while (true) {
            try {
                attempts++;
                Socket socket = socket();
                socket.getOutputStream().write(m.marshell());
                socket.getOutputStream().flush();
                break;
            } catch (IOException e) {
                if (attempts >= RETRIES) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
