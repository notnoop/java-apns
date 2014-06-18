package com.notnoop.apns;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import javax.net.ssl.SSLContext;

/**
 * Represents the Apple APNS server. This allows testing outside of the Apple
 * servers.
 */
@SuppressWarnings("deprecation")
public class ApnsGatewayServerSocket extends AbstractApnsServerSocket {
	private final ApnsServerService apnsServerService;

	public ApnsGatewayServerSocket(SSLContext sslContext, int port,
			ExecutorService executorService,
			ApnsServerService apnsServerService,
			ApnsServerExceptionDelegate exceptionDelegate) throws IOException {
		super(sslContext, port, executorService, exceptionDelegate);
		this.apnsServerService = apnsServerService;
	}

	@Override
	void handleSocket(Socket socket) throws IOException {
		InputStream inputStream = socket.getInputStream();
		DataInputStream dataInputStream = new DataInputStream(inputStream);
		while (true) {
			int identifier = 0;
			try {
				int read = dataInputStream.read();
				if (read == -1) {
					break;
				}

				boolean enhancedFormat = read == 1;
				int expiry = 0;
				if (enhancedFormat) {
					identifier = dataInputStream.readInt();
					expiry = dataInputStream.readInt();
				}

				int deviceTokenLength = dataInputStream.readShort();
				byte[] deviceTokenBytes = toArray(inputStream,
						deviceTokenLength);

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
			} catch (IOException ioe) {
				writeResponse(socket, identifier, 8, 1);
				break;
			} catch (Exception e) {
				writeResponse(socket, identifier, 8, 1);
				break;
			}
		}
	}

	private void writeResponse(Socket socket, int identifier, int command,
			int status) {
		try {
			BufferedOutputStream bos = new BufferedOutputStream(
					socket.getOutputStream());
			DataOutputStream dataOutputStream = new DataOutputStream(bos);
			dataOutputStream.writeByte(command);
			dataOutputStream.writeByte(status);
			dataOutputStream.writeInt(identifier);
			dataOutputStream.flush();
		} catch (IOException ioe) {
			// if we can't write a response, nothing we can do
		}
	}

	private byte[] toArray(InputStream inputStream, int size)
			throws IOException {
		byte[] bytes = new byte[size];
        final DataInputStream dis = new DataInputStream(inputStream);
        dis.readFully(bytes);
		return bytes;
	}
}