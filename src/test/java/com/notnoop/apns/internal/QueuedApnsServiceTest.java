package com.notnoop.apns.internal;

import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Mockito.*;

import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.ApnsService;

public class QueuedApnsServiceTest extends ApnsServiceImplTest{

    @Test(expected=IllegalStateException.class)
    public void sendWithoutStarting() {
        QueuedApnsService service = new QueuedApnsService(null);
        service.push(notification);
    }

    @Test
    public void dontBlock() {
        final int delay = 10000;
        ApnsConnection connection = new ApnsConnection(null, null, 0) {
            protected synchronized void sendMessage(ApnsNotification m) {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                }
            }
        };

        QueuedApnsService queued =
            new QueuedApnsService(new ApnsServiceImpl(connection, null));
        queued.start();
        long time1 = System.currentTimeMillis();
        queued.push(notification);
        queued.push(notification);
        long time2 = System.currentTimeMillis();
        assertTrue("Queued blocks unexpectedly", (time2- time1) < delay);

        queued.stop();
    }

    protected ApnsService newService(ApnsConnection connection, ApnsFeedbackConnection feedback) {
        ApnsService service = new ApnsServiceImpl(connection, null);
        ApnsService queued = new QueuedApnsService(service);
        queued.start();
        return queued;
    }
}
