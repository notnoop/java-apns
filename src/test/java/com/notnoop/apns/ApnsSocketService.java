package com.notnoop.apns;

import java.io.IOException;

public class ApnsSocketService {
	private final AbstractApnsServerSocket apnsPushServerSocket;
	private final AbstractApnsServerSocket apnsFeedbackServerSocket;

	public ApnsSocketService(AbstractApnsServerSocket apnsPushServerSocket,
			AbstractApnsServerSocket apnsFeedbackServerSocket)
			throws IOException {
		this.apnsPushServerSocket = apnsPushServerSocket;
		this.apnsFeedbackServerSocket = apnsFeedbackServerSocket;
	}

	public void start() {
		apnsPushServerSocket.start();
		apnsFeedbackServerSocket.start();
	}

	public void stop() {
		apnsPushServerSocket.stop();
		apnsFeedbackServerSocket.stop();
	}
}