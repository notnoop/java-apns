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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A Server simulator that can simulate some failure modes.
 */
public class FailingApnsServerSimulator extends ApnsServerSimulator {

    private static final Logger logger = LoggerFactory.getLogger(FailingApnsServerSimulator.class);


    private BlockingQueue<Notification> queue = new LinkedBlockingQueue<Notification>();

    public FailingApnsServerSimulator(final ServerSocketFactory sslFactory) {
        super(sslFactory);
    }

    @Override
    protected void onNotification(final ApnsServerSimulator.Notification notification, final InputOutputSocket inputOutputSocket)
            throws IOException {
        logger.debug("Queueing notification " + notification);
        queue.add(notification);
        final byte[] token = notification.getDeviceToken();
        if (token.length == 32 && token[0] == (byte)0xff && token[1] == (byte)0xff) {
            switch (token[2]) {
                case 0:
                    fail(token[3], notification.getIdentifier(), inputOutputSocket);
                    break;

                case 1:
                    try {
                        final int millis = token[3] * 100;
                        Thread.sleep(millis);
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                    }
                    break;

                case 2:
                default:
                    inputOutputSocket.close();
                    break;
            }

        }
    }

    protected List<byte[]> getBadTokens() {
        return super.getBadTokens();
    }

    public BlockingQueue<Notification> getQueue() {
        return queue;
    }

}
