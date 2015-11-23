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

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * Represents the Apple server. This allows testing outside of the Apple
 * servers. Sub-classes should implement the specific handing of new socket
 * connections.
 */
public abstract class AbstractApnsServerSocket {
	private final SSLServerSocket serverSocket;
	private final ExecutorService executorService;
	private final ApnsServerExceptionDelegate exceptionDelegate;

	public AbstractApnsServerSocket(SSLContext sslContext, int port,
			ExecutorService executorService,
			ApnsServerExceptionDelegate exceptionDelegate) throws IOException {
		SSLServerSocketFactory serverSocketFactory = sslContext
				.getServerSocketFactory();
		serverSocket = (SSLServerSocket) serverSocketFactory
				.createServerSocket(port);
		this.executorService = executorService;
		this.exceptionDelegate = exceptionDelegate;
	}

	/**
	 * Start the server accept process. This method is non-blocking.
	 */
	public final void start() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				startAccept();
			}
		}).start();
	}

	@SuppressWarnings("InfiniteLoopStatement")
    private void startAccept() {

		try {
			while (true) {
				Socket accept = serverSocket.accept();
                // Work around JVM deadlock ... https://community.oracle.com/message/10989561#10989561
                accept.setSoLinger(true, 1);
                executorService.execute(new SocketHandler(accept));
			}
		} catch (IOException ioe) {
			executorService.shutdown();
		}
	}

	/**
	 * Stops the server socket. This method is blocking.
	 */
	public final void stop() {
		try {
			serverSocket.close();
		} catch (IOException ioe) {
			// don't care
		}

		executorService.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
				executorService.shutdownNow(); // Cancel currently executing
												// tasks
				// Wait a while for tasks to respond to being cancelled
				if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
					System.err.println("Pool did not terminate");
				}
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			executorService.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

	private class SocketHandler implements Runnable {
		private final Socket socket;

		SocketHandler(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				handleSocket(socket);
			} catch (IOException ioe) {
				exceptionDelegate.handleRequestFailed(ioe);
			}
		}
	}

	abstract void handleSocket(Socket socket) throws IOException;
}
