package com.notnoop.apns.internal;

import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.ApnsService;

public class QueuedApnsServiceTest extends ApnsServiceImplTest{

    ApnsNotification notification = new ApnsNotification("2342", "{}");

    protected ApnsService newService(ApnsConnection connection, ApnsFeedbackConnection feedback) {
        ApnsService service = new ApnsServiceImpl(connection, null);
        ApnsService queued = new QueuedApnsService(service);
        queued.start();
        return queued;
    }
}
