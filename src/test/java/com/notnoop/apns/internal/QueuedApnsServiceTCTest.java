package com.notnoop.apns.internal;

import static org.junit.Assert.*;

import org.junit.Test;
import static org.mockito.Mockito.*;

import com.notnoop.apns.ApnsService;
import com.notnoop.apns.SimpleApnsNotification;
import com.notnoop.apns.internal.QueuedApnsServiceTest.ConnectionStub;

public class QueuedApnsServiceTCTest {

    @Test(expected=IllegalStateException.class)
    public void sendWithoutStarting() {
        QueuedApnsService service = new QueuedApnsService(null);
        service.push(notification);
    }

    SimpleApnsNotification notification = new SimpleApnsNotification("2342", "{}");

    @Test
    public void pushEvantually() {
        ConnectionStub connection = spy(new ConnectionStub(0, 1));
        ApnsService service = newService(connection, null);

        service.push(notification);
        connection.semaphor.acquireUninterruptibly();

        verify(connection, times(1)).sendMessage(notification);
    }

    @Test
    public void dontBlock() {
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
        connection.semaphor.acquireUninterruptibly();
        verify(connection, times(2)).sendMessage(notification);

        queued.stop();
    }

    protected ApnsService newService(ApnsConnection connection, ApnsFeedbackConnection feedback) {
        ApnsService service = new ApnsServiceImpl(connection, null);
        ApnsService queued = new QueuedApnsService(service);
        queued.start();
        return queued;
    }
}
