package com.notnoop.apns.utils.Simulator;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Wrap some of the boilerplate code using socket, enable passing around a socket together with its streams.
 */
public class InputOutputSocket {
    private final Socket socket;
    private final ApnsInputStream inputStream;
    private final DataOutputStream outputStream;

    public InputOutputSocket(final Socket socket) throws IOException {
        if (socket == null) {
            throw new NullPointerException("socket may not be null");
        }

        this.socket = socket;

        // Hack, work around JVM deadlock ... https://community.oracle.com/message/10989561#10989561
        socket.setSoLinger(true, 1);
        outputStream = new DataOutputStream(socket.getOutputStream());
        inputStream = new ApnsInputStream(socket.getInputStream());
    }

    public Socket getSocket() {
        return socket;
    }

    public ApnsInputStream getInputStream() {
        return inputStream;
    }

    /*
    public DataOutputStream getOutputStream() {
        return outputStream;
    }
    */



    public synchronized void close() {
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write data to the output stream while synchronized against close(). This hopefully fixes
     * sporadic test failures caused by a deadlock of write() and close()
     * @param bytes The data to write
     * @throws IOException if an error occurs
     */
    public void syncWrite(byte[] bytes) throws IOException {
        outputStream.write(bytes);
        outputStream.flush();
    }
}
