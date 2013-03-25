package com.notnoop.apns;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import javax.net.ssl.SSLContext;

/**
 * Represents the Apple APNS server. This allows testing outside of the Apple
 * servers.
 */
public class ApnsGatewayServerSocket extends AbstractApnsServerSocket {
	private final ApnsServerService apnsServerService;

	public ApnsGatewayServerSocket(SSLContext sslContext, int port,
			ExecutorService executorService, ApnsServerService apnsServerService,
			ApnsServerExceptionDelegate exceptionDelegate) throws IOException {
		super(sslContext, port, executorService, exceptionDelegate);
		this.apnsServerService = apnsServerService;
	}

	@Override
	void handleSocket(Socket socket) throws IOException {
		int identifier = 0;
		try {
			InputStream inputStream = socket.getInputStream();
			DataInputStream dataInputStream = new DataInputStream(inputStream);

			boolean enhancedFormat = dataInputStream.read() == 1;
			int expiry = 0;
			if (enhancedFormat) {
				identifier = dataInputStream.readInt();
				expiry = dataInputStream.readInt();
			}

			int deviceTokenLength = dataInputStream.readShort();
			byte[] deviceTokenBytes = toArray(inputStream, deviceTokenLength);

			int payloadLength = dataInputStream.readShort();
			byte[] payloadBytes = toArray(inputStream, payloadLength);

			ApnsNotification message;
			if (enhancedFormat) {
				message = new EnhancedApnsNotification(identifier, expiry,
						deviceTokenBytes, payloadBytes);
			} else {
				message = new SimpleApnsNotification(deviceTokenBytes,
						payloadBytes);
			}
			apnsServerService.messageReceived(message);
			writeResponse(socket, identifier, 0, 0);
		} catch (IOException ioe) {
			writeResponse(socket, identifier, 8, 1);
			throw ioe;
		} catch (Exception expt) {
			writeResponse(socket, identifier, 8, 1);
			throw new IOException("Failed to handle socket", expt);
		}
	}

	private void writeResponse(Socket socket, int identifier, int command,
			int status) {
		try {
			OutputStream outputStream = socket.getOutputStream();
			DataOutputStream dataOutputStream = new DataOutputStream(
					outputStream);
			dataOutputStream.writeByte(command);
			dataOutputStream.writeByte(status);
			dataOutputStream.write(identifier);
			dataOutputStream.flush();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			// failed to write response
		}
	}

	private byte[] toArray(InputStream inputStream, int size)
			throws IOException {
		byte[] bytes = new byte[size];
		inputStream.read(bytes);
		return bytes;
	}
}