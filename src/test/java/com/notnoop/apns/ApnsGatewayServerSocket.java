/*
 *  Copyright 2009, Mahmood Ali.
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 *
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following disclaimer
 *      in the documentation and/or other materials provided with the
 *      distribution.
 *    * Neither the name of Mahmood Ali. nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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