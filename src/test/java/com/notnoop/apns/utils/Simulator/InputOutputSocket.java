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
package com.notnoop.apns.utils.Simulator;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrap some of the boilerplate code using socket, enable passing around a socket together with its streams.
 */
public class InputOutputSocket {
    private static final Logger LOGGER = LoggerFactory.getLogger(InputOutputSocket.class);
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
            LOGGER.warn("Can not close inputStream properly", e);
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            LOGGER.warn("Can not close outputStream properly", e);
        }

        try {
            socket.close();
        } catch (IOException e) {
            LOGGER.warn("Can not close socket properly", e);
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
