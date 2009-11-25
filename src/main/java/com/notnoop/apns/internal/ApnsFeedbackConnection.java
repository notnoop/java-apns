package com.notnoop.apns.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Date;
import java.util.Map;

import javax.net.SocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApnsFeedbackConnection {
    private static final Logger logger = LoggerFactory.getLogger(ApnsFeedbackConnection.class);

    private final SocketFactory factory;
    private final String host;
    private final int port;

    public ApnsFeedbackConnection(SocketFactory factory, String host, int port) {
    	this.factory = factory;
    	this.host = host;
    	this.port = port;
    }

    public Map<String, Date> getInactiveDevices() {
    	Socket socket = null;
    	try {
			socket = factory.createSocket(host, port);
			InputStream stream = socket.getInputStream();
			return Utilities.parseFeedbackStream(stream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (socket != null)
					socket.close();
			} catch (Exception e) {}
		}
    }

}
