package com.notnoop.apns;

import java.io.IOException;
import java.net.Socket;

import javax.net.ssl.SSLContext;

public class ApnsFeedbackServerSocket extends AbstractApnsServerSocket {

	public ApnsFeedbackServerSocket(SSLContext sslContext,
			ApnsServerExceptionDelegate exceptionDelegate) throws IOException {
		super(sslContext, 2196, exceptionDelegate);
	}

	@Override
	void handleSocket(Socket socket) throws IOException {
		socket.getOutputStream().close();
	}
}