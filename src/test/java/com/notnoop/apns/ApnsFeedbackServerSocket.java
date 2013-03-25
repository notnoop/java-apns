package com.notnoop.apns;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import javax.net.ssl.SSLContext;

/**
 * Represents the Apple Feedback server. This allows testing outside of the
 * Apple servers.
 */
public class ApnsFeedbackServerSocket extends AbstractApnsServerSocket {
	private final ApnsServerService requestDelegate;

	public ApnsFeedbackServerSocket(SSLContext sslContext, int port,
			ExecutorService executorService, ApnsServerService requestDelegate,
			ApnsServerExceptionDelegate exceptionDelegate) throws IOException {
		super(sslContext, port, executorService, exceptionDelegate);
		this.requestDelegate = requestDelegate;
	}

	@Override
	void handleSocket(Socket socket) throws IOException {
		Map<byte[], Date> inactiveDevices = requestDelegate
				.getInactiveDevices();
		DataOutputStream dataStream = new DataOutputStream(
				socket.getOutputStream());
		for (Entry<byte[], Date> entry : inactiveDevices.entrySet()) {
			int time = (int) (entry.getValue().getTime() / 1000L);
			dataStream.writeInt(time);
			byte[] bytes = entry.getKey();
			dataStream.writeShort(bytes.length);
			dataStream.write(bytes);
		}
		dataStream.close();
	}
}