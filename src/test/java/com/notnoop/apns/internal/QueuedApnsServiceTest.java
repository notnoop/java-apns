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
