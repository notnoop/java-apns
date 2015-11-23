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
package com.notnoop.apns.internal;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import org.junit.Test;
import static org.mockito.Mockito.*;

import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.EnhancedApnsNotification;
import com.notnoop.exceptions.NetworkIOException;

public class QueuedApnsServiceTest {

    @Test(expected = IllegalStateException.class)
    public void sendWithoutStarting() {
        QueuedApnsService service = new QueuedApnsService(null);
        service.push(notification);
    }
    EnhancedApnsNotification notification = new EnhancedApnsNotification(1,
            EnhancedApnsNotification.MAXIMUM_EXPIRY, "2342", "{}");

    @Test
    public void pushEventually() {
        ConnectionStub connection = spy(new ConnectionStub(0, 1));
        ApnsService service = newService(connection, null);

        service.push(notification);
        connection.semaphore.acquireUninterruptibly();

        verify(connection, times(1)).sendMessage(notification);
    }

    @Test
    public void pushEventuallySample() {
        ConnectionStub connection = spy(new ConnectionStub(0, 1));
        ApnsService service = newService(connection, null);

        service.push("2342", "{}");
        connection.semaphore.acquireUninterruptibly();

        verify(connection, times(1)).sendMessage(notification);
    }

    @Test
    public void doNotBlock() {
        final int delay = 10000;
        ConnectionStub connection = spy(new ConnectionStub(delay, 2));
        QueuedApnsService queued =
                new QueuedApnsService(new ApnsServiceImpl(connection, null));
        queued.start();
        long time1 = System.currentTimeMillis();
        queued.push(notification);
        queued.push(notification);
        long time2 = System.currentTimeMillis();
        assertTrue("queued.push() blocks", (time2 - time1) < delay);

        connection.interrupt();
        connection.semaphore.acquireUninterruptibly();
        verify(connection, times(2)).sendMessage(notification);

        queued.stop();
    }

    protected ApnsService newService(ApnsConnection connection, ApnsFeedbackConnection feedback) {
        ApnsService service = new ApnsServiceImpl(connection, null);
        ApnsService queued = new QueuedApnsService(service);
        queued.start();
        return queued;
    }

    static class ConnectionStub implements ApnsConnection {

        Semaphore semaphore;
        int delay;

        public ConnectionStub(int delay, int expectedCalls) {
            this.semaphore = new Semaphore(1 - expectedCalls);
            this.delay = delay;
        }
        volatile boolean stop;

        public synchronized void sendMessage(ApnsNotification m) {
            long time = System.currentTimeMillis();
            while (!stop && (System.currentTimeMillis() < time + delay)) {
                // WTF? Here was a busy wait for up to 10 seconds or until "stop" fired. Added: A tiny amount of sleep every round.
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            semaphore.release();
        }

        protected void interrupt() {
            stop = true;
        }

        public ApnsConnection copy() {
            throw new RuntimeException("Not implemented");
        }

        public void close() throws IOException {
        }

        public void testConnection() throws NetworkIOException {
        }

        public void setCacheLength(int cacheLength) {
        }

        public int getCacheLength() {
            return -1;
        }
    }
}
