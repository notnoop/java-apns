package com.notnoop.apns.utils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ServerSocketFactory;

public class ApnsServerStub {

    public static ApnsServerStub prepareAndStartServer(int gatePort, int feedPort) {
        ApnsServerStub server = new ApnsServerStub(
                FixedCertificates.serverContext().getServerSocketFactory(),
                gatePort, feedPort);
        server.start();
        return server;
    }


    public final ByteArrayOutputStream received;
    public final ByteArrayOutputStream toSend;
    public final Semaphore semaphore = new Semaphore(0);

    private final ServerSocketFactory sslFactory;
    private final int gatewayPort, feedbackPort;

    public ApnsServerStub(ServerSocketFactory sslFactory,
            int gatewayPort, int feedbackPort) {
        this.sslFactory = sslFactory;
        this.gatewayPort = gatewayPort;
        this.feedbackPort = feedbackPort;

        this.received = new ByteArrayOutputStream();
        this.toSend = new ByteArrayOutputStream();
    }

    Thread gatewayThread, feedbackThread;
    ServerSocket gatewaySocket, feedbackSocket;

    public void start() {
        gatewayThread = new Thread(new GatewayRunner());
        feedbackThread = new Thread(new FeedbackRunner());
        gatewayThread.start();
        feedbackThread.start();
    }

    @SuppressWarnings("deprecation")
    public void stop() {
        try {
            gatewaySocket.close();
            feedbackSocket.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        gatewayThread.stop();
        feedbackThread.stop();
    }

    private class GatewayRunner implements Runnable {

        @Override
        public void run() {
            try {
                gatewaySocket = sslFactory.createServerSocket(gatewayPort);
            } catch (IOException e) {
                semaphore.release();
                throw new RuntimeException(e);
            }

            try {
                // Listen for connections
                Socket socket = gatewaySocket.accept();

                // Create streams to securely send and receive data to the client
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();

                // Read from in and write to out...
                byte[] read = readFully(in);
                received.write(read);
                semaphore.release();

                // Close the socket
                in.close();
                out.close();
            } catch(IOException e) {
                semaphore.release();
            }
        }
    }

    private class FeedbackRunner implements Runnable {

        @Override
        public void run() {
            try {
                feedbackSocket = sslFactory.createServerSocket(feedbackPort);
            } catch (IOException e) {
                e.printStackTrace();
                semaphore.release();
                throw new RuntimeException(e);
            }

            try {
                // Listen for connections
                Socket socket = feedbackSocket.accept();

                // Create streams to securely send and receive data to the client
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();

                // Read from in and write to out...
                toSend.writeTo(out);
                semaphore.release();

                // Close the socket
                in.close();
                out.close();
            } catch(IOException e) {
            }
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
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return stream.toByteArray();
    }
}
