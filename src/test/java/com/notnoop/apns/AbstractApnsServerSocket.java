package com.notnoop.apns;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public abstract class AbstractApnsServerSocket {
	private final SSLServerSocket serverSocket;
	private final ExecutorService executorService;
	private final ApnsServerExceptionDelegate exceptionDelegate;

	public AbstractApnsServerSocket(SSLContext sslContext, int port,
			ApnsServerExceptionDelegate exceptionDelegate) throws IOException {
		SSLServerSocketFactory serverSocketFactory = sslContext
				.getServerSocketFactory();
		serverSocket = (SSLServerSocket) serverSocketFactory
				.createServerSocket(port);
		executorService = new ThreadPoolExecutor(20, Integer.MAX_VALUE, 60L,
				TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
		this.exceptionDelegate = exceptionDelegate;
	}

	public final void start() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				startAccept();
			}
		}).start();
	}

	private void startAccept() {

		try {
			while (true) {
				Socket accept = serverSocket.accept();
				executorService.execute(new SocketHandler(accept));
			}
		} catch (IOException ioe) {
			executorService.shutdown();
		}
	}

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
