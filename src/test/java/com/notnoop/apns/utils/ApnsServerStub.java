package com.notnoop.apns.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.ServerSocketFactory;

public class ApnsServerStub {

    /**
     * Create an ApnsServerStub
     *
     * @param gatePort port for the gateway stub server
     * @param feedPort port for the feedback stub server
     * @return an ApnsServerStub
     * @deprecated use prepareAndStartServer() without port numbers and query the port numbers from the server using
     * ApnsServerStub.getEffectiveGatewayPort() and ApnsServerStub.getEffectiveFeedbackPort()
     */
    @Deprecated
    public static ApnsServerStub prepareAndStartServer(int gatePort, int feedPort) {
        ApnsServerStub server = new ApnsServerStub(FixedCertificates.serverContext().getServerSocketFactory(), gatePort, feedPort);
        server.start();
        return server;
    }

    /**
     * Create an ApnsServerStub that uses any free port for gateway and feedback.
     *
     * @return the server stub. Use getEffectiveGatewayPort() and getEffectiveFeedbackPort() to ask for ports.
     */
    public static ApnsServerStub prepareAndStartServer() {
        ApnsServerStub server = new ApnsServerStub(FixedCertificates.serverContext().getServerSocketFactory());
        server.start();
        return server;
    }

    private final AtomicInteger toWaitBeforeSend = new AtomicInteger(0);
    private final ByteArrayOutputStream received;
    private final ByteArrayOutputStream toSend;
    private final Semaphore messages = new Semaphore(0);
    private final Semaphore startUp = new Semaphore(0);
    private final Semaphore gatewayOutLock = new Semaphore(0);
    private final Semaphore waitForError = new Semaphore(1);
    private final ServerSocketFactory sslFactory;
    private final int gatewayPort;
    private final int feedbackPort;
    private int effectiveGatewayPort;
    private int effectiveFeedbackPort;
    private OutputStream gatewayOutputStream = null;

    public ApnsServerStub(ServerSocketFactory sslFactory) {
        this(sslFactory, 0, 0);
    }

    public ApnsServerStub(ServerSocketFactory sslFactory, int gatewayPort, int feedbackPort) {
        this.sslFactory = sslFactory;
        this.gatewayPort = gatewayPort;
        this.feedbackPort = feedbackPort;

        this.received = new ByteArrayOutputStream();
        this.toSend = new ByteArrayOutputStream();
    }

    Thread gatewayThread;
    Thread feedbackThread;
    ServerSocket gatewaySocket;
    ServerSocket feedbackSocket;

    public void start() {
        gatewayThread = new GatewayRunner();
        feedbackThread = new FeedbackRunner();
        gatewayThread.start();
        feedbackThread.start();
        startUp.acquireUninterruptibly(2);
    }

    @SuppressWarnings("deprecation")
    public void stop() {
        try {
            if (gatewaySocket != null) {
                gatewaySocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (feedbackSocket != null) {
                feedbackSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (gatewayThread != null) {
            gatewayThread.stop();
        }

        if (feedbackThread != null) {
            feedbackThread.stop();
        }

    }

    public void sendError(int err, int id) {
        ByteBuffer buf = ByteBuffer.allocate(6);
        buf.put((byte) 8).put((byte) err).putInt(id);
        try {
            gatewayOutLock.acquire();
            gatewayOutputStream.write(buf.array());
            gatewayOutputStream.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int getEffectiveGatewayPort() {
        return effectiveGatewayPort;
    }

    public int getEffectiveFeedbackPort() {
        return effectiveFeedbackPort;
    }

    public AtomicInteger getToWaitBeforeSend() {
        return toWaitBeforeSend;
    }

    public ByteArrayOutputStream getReceived() {
        return received;
    }

    public ByteArrayOutputStream getToSend() {
        return toSend;
    }

    public Semaphore getMessages() {
        return messages;
    }

    public Semaphore getWaitForError() {
        return waitForError;
    }

    private class GatewayRunner extends Thread {

        @SuppressWarnings("InfiniteLoopStatement")
        public void run() {


            try {
                gatewaySocket = sslFactory.createServerSocket(gatewayPort);
            } catch (IOException e) {
                messages.release();
                throw new RuntimeException(e);
            }

            InputStream in = null;
            effectiveGatewayPort = gatewaySocket.getLocalPort();

            try {
                // Listen for connections
                startUp.release();
                while (true) {
                    Socket socket = gatewaySocket.accept();
                    // Work around JVM deadlock ... https://community.oracle.com/message/10989561#10989561
                    socket.setSoLinger(true, 1);

                    // Create streams to securely send and receive data to the client
                    in = socket.getInputStream();
                    gatewayOutputStream = socket.getOutputStream();
                    gatewayOutLock.release();

                    // Read from in and write to out...
                    byte[] read = readFully(in);

                    waitBeforeSend();
                    received.write(read);
                    messages.release();


                    waitForError.acquire();

                    // Close the socket
                    in.close();
                    gatewayOutputStream.close();
                }
            } catch (Throwable e) {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ioex) {
                    System.err.println(ioex.toString());
                }
                try {
                    if (gatewayOutputStream != null) {
                        gatewayOutputStream.close();
                    }
                } catch (IOException ioex) {
                    System.err.println(ioex.toString());
                }
                messages.release();
            }
        }

    }

    private class FeedbackRunner extends Thread {

        public void run() {
            try {
                feedbackSocket = sslFactory.createServerSocket(feedbackPort);
            } catch (IOException e) {
                e.printStackTrace();
                messages.release();
                throw new RuntimeException(e);
            }

            effectiveFeedbackPort = feedbackSocket.getLocalPort();
            try {
                // Listen for connections
                startUp.release();
                Socket socket = feedbackSocket.accept();
                // Work around JVM deadlock ... https://community.oracle.com/message/10989561#10989561
                socket.setSoLinger(true, 1);

                // Create streams to securely send and receive data to the client
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();

                waitBeforeSend();
                // Read from in and write to out...
                toSend.writeTo(out);

                // Close the socket
                in.close();
                out.close();
            } catch (SocketException se) {
                // Ignore closed socket.
            } catch (IOException ioex) {
                ioex.printStackTrace();
            }
            messages.release();
        }
    }

    AtomicInteger readLen = new AtomicInteger();

    public void stopAt(int length) {
        readLen.set(length);
    }

    public byte[] readFully(InputStream st) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        int read;
        try {
            while (readLen.getAndDecrement() > 0 && (read = st.read()) != -1) {
                stream.write(read);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return stream.toByteArray();
    }

    /**
     * Introduces a waiting time, used to trigger read timeouts.
     */
    private void waitBeforeSend() {
        int wait = toWaitBeforeSend.get();
        if (wait != 0) {
            try {
                Thread.sleep(wait);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
