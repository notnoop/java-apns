package com.notnoop.apns;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.net.ssl.SSLContext;

import com.notnoop.apns.internal.Utilities;

public class ApnsServerSocket extends AbstractApnsServerSocket {
	private final ApnsRequestDelegate requestDelegate;

	public ApnsServerSocket(SSLContext sslContext,
			ApnsRequestDelegate requestDelegate,
			ApnsServerExceptionDelegate exceptionDelegate) throws IOException {
		this(sslContext, 2195, requestDelegate, exceptionDelegate);
	}

	public ApnsServerSocket(SSLContext sslContext, int port,
			ApnsRequestDelegate requestDelegate,
			ApnsServerExceptionDelegate exceptionDelegate) throws IOException {
		super(sslContext, port, exceptionDelegate);
		this.requestDelegate = requestDelegate;
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
			String deviceToken = Utilities.encodeHex(deviceTokenBytes);

			int payloadLength = dataInputStream.readShort();
			byte[] payloadBytes = toArray(inputStream, payloadLength);
			String payload = new String(payloadBytes, "UTF-8");

			ApnsRequest message = new ApnsRequest(identifier, expiry,
					deviceToken, payload);
			requestDelegate.messageReceived(message);
			writeResponse(socket, identifier, 0, 0);
		} catch (IOException ioe) {
			writeResponse(socket, identifier, 8, 1);
			throw ioe;
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