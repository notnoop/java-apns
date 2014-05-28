package com.notnoop.apns.utils.Simulator;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import com.notnoop.apns.utils.Simulator.ApnsInputStream;

/**
 * Wrap some of the boilerplate code using socket, enable passing around a socket together with its streams.
 */
public class InputOutputSocket {
    private final Socket socket;
    private final ApnsInputStream inputStream;
    private final DataOutputStream outputStream;

    public InputOutputSocket(final Socket socket) throws IOException {
        this.socket = socket;
        outputStream = new DataOutputStream(socket.getOutputStream());
        inputStream = new ApnsInputStream(socket.getInputStream());
    }

    public Socket getSocket() {
        return socket;
    }

    public ApnsInputStream getInputStream() {
        return inputStream;
    }

    public DataOutputStream getOutputStream() {
        return outputStream;
    }

    public synchronized void close() {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
