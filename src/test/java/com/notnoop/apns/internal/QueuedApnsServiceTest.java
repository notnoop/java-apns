package com.notnoop.apns.internal;

import org.junit.Test;

import com.notnoop.apns.ApnsService;

public class QueuedApnsServiceTest extends ApnsServiceImplTest{

    @Test(expected=IllegalStateException.class)
    public void sendWithoutStarting() {
        QueuedApnsService service = new QueuedApnsService(null);
        service.push(notification);
    }

    protected ApnsService newService(ApnsConnection connection, ApnsFeedbackConnection feedback) {
        ApnsService service = new ApnsServiceImpl(connection, null);
        ApnsService queued = new QueuedApnsService(service);
        queued.start();
        return queued;
    }
}
